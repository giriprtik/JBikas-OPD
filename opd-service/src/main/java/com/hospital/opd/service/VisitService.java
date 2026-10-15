package com.hospital.opd.service;

import com.hospital.opd.client.LabClient;
import com.hospital.opd.client.LabOrderCreated;
import com.hospital.opd.client.LabOrderPayload;
import com.hospital.opd.dto.*;
import com.hospital.opd.exception.BusinessRuleException;
import com.hospital.opd.exception.NotFoundException;
import com.hospital.opd.model.*;
import com.hospital.opd.repository.VisitEventRepository;
import com.hospital.opd.repository.VisitRepository;
import com.hospital.opd.repository.VitalSignsRepository;
import com.hospital.opd.security.CurrentUser;
import com.hospital.opd.security.StaffPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OPD visit workflow. All state changes go through {@link #moveTo} which enforces
 * {@link VisitStatus#canMoveTo} and writes an audit event.
 */
@Service
public class VisitService {

    private static final Comparator<Visit> QUEUE_ORDER = Comparator
            .comparingInt((Visit v) -> v.getPriority().rank())
            .thenComparing(Visit::getRegisteredAt);

    private final VisitRepository visits;
    private final VitalSignsRepository vitalsRepo;
    private final VisitEventRepository events;
    private final PatientService patientService;
    private final VitalsAssessor vitalsAssessor;
    private final LabClient labClient;
    private final OpdMapper mapper;
    private final SequenceService sequences;
    private final Clock clock;

    public VisitService(VisitRepository visits, VitalSignsRepository vitalsRepo, VisitEventRepository events,
                        PatientService patientService, VitalsAssessor vitalsAssessor, LabClient labClient,
                        OpdMapper mapper, SequenceService sequences, Clock clock) {
        this.visits = visits;
        this.vitalsRepo = vitalsRepo;
        this.events = events;
        this.patientService = patientService;
        this.vitalsAssessor = vitalsAssessor;
        this.labClient = labClient;
        this.mapper = mapper;
        this.sequences = sequences;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- reads

    @Transactional(readOnly = true)
    public List<VisitSummaryResponse> listForDay(LocalDate date, Set<VisitStatus> statuses) {
        LocalDate day = date != null ? date : LocalDate.now(clock);
        List<Visit> found = statuses == null || statuses.isEmpty()
                ? visits.findByVisitDateOrderByTokenNumberAsc(day)
                : visits.findByVisitDateAndStatusInOrderByTokenNumberAsc(day, statuses);
        return summaries(found);
    }

    /**
     * Doctor's queue: every open visit, emergencies first, then by arrival. Un-triaged patients appear
     * only if they were registered as EMERGENCY (they may be seen before vitals are taken).
     */
    @Transactional(readOnly = true)
    public List<VisitSummaryResponse> doctorQueue() {
        List<Visit> open = visits.findByStatusIn(VisitStatus.ACTIVE).stream()
                .filter(v -> v.getStatus() != VisitStatus.REGISTERED || v.getPriority() == TriagePriority.EMERGENCY)
                .sorted(QUEUE_ORDER)
                .toList();
        return summaries(open);
    }

    @Transactional(readOnly = true)
    public VisitDetailResponse get(Long id) {
        Visit visit = find(id);
        return detail(visit);
    }

    // ---------------------------------------------------------------- nurse actions

    @Transactional
    public VisitDetailResponse open(VisitCreateRequest request) {
        Patient patient = patientService.find(request.patientId());
        if (visits.existsByPatientIdAndStatusIn(patient.getId(), VisitStatus.ACTIVE)) {
            throw new BusinessRuleException(patient.getFullName() + " (" + patient.getMrn()
                    + ") already has an open visit. Complete or cancel it before opening a new one.");
        }
        LocalDate today = LocalDate.now(clock);
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitDate(today);
        visit.setTokenNumber(sequences.nextToken(today));
        visit.setChiefComplaint(request.chiefComplaint().trim());
        visit.setPriority(request.priority() != null ? request.priority() : TriagePriority.NORMAL);
        visit.setStatus(VisitStatus.REGISTERED);
        visit.setRegisteredAt(Instant.now(clock));
        visit.setRegisteredBy(CurrentUser.username());
        visits.save(visit);
        log(visit, null, VisitStatus.REGISTERED, "Token " + visit.getTokenNumber() + ", priority " + visit.getPriority());
        return detail(visit);
    }

    /**
     * Records a set of vitals. The first set moves the visit from REGISTERED to TRIAGED.
     * A critical vital raises a NORMAL priority to URGENT so the patient moves up the doctor's queue.
     */
    @Transactional
    public VisitDetailResponse recordVitals(Long id, TriageRequest r) {
        Visit visit = find(id);
        requireActive(visit);

        VitalSigns v = new VitalSigns();
        v.setVisit(visit);
        v.setTemperatureC(r.temperatureC());
        v.setPulseBpm(r.pulseBpm());
        v.setRespiratoryRate(r.respiratoryRate());
        v.setSystolicBp(r.systolicBp());
        v.setDiastolicBp(r.diastolicBp());
        v.setSpo2(r.spo2());
        v.setWeightKg(r.weightKg());
        v.setHeightCm(r.heightCm());
        v.setRecordedBy(CurrentUser.username());
        v.setRecordedAt(Instant.now(clock));
        vitalsRepo.save(v);

        int age = AgeCalculator.years(visit.getPatient().getDateOfBirth(), LocalDate.now(clock));
        Map<String, VitalFlag> flags = vitalsAssessor.assess(v, age);

        TriagePriority requested = r.priority() != null ? r.priority() : visit.getPriority();
        if (VitalsAssessor.anyCritical(flags) && requested == TriagePriority.NORMAL) {
            requested = TriagePriority.URGENT;
        }
        String priorityNote = changePriority(visit, requested);

        if (visit.getStatus() == VisitStatus.REGISTERED) {
            moveTo(visit, VisitStatus.TRIAGED, joinNotes("Vitals recorded", priorityNote));
        } else {
            log(visit, visit.getStatus(), visit.getStatus(), joinNotes("Vitals re-recorded", priorityNote));
        }
        return detail(visit);
    }

    @Transactional
    public VisitDetailResponse cancel(Long id, CancelRequest request) {
        Visit visit = find(id);
        if (!visit.getStatus().canMoveTo(VisitStatus.CANCELLED)) {
            throw new BusinessRuleException("Only visits that have not reached the doctor can be cancelled (current status: "
                    + visit.getStatus() + ").");
        }
        visit.setCancelReason(request.reason().trim());
        moveTo(visit, VisitStatus.CANCELLED, request.reason().trim());
        return detail(visit);
    }

    // ---------------------------------------------------------------- doctor actions

    /** Claims the patient for the signed-in doctor. Re-entering an own consultation is a no-op. */
    @Transactional
    public VisitDetailResponse startConsultation(Long id) {
        Visit visit = find(id);
        StaffPrincipal doctor = CurrentUser.get();

        if (visit.getStatus() == VisitStatus.IN_CONSULTATION) {
            if (doctor.username().equals(visit.getAssignedDoctor())) {
                return detail(visit);
            }
            throw new BusinessRuleException("This patient is currently with " + visit.getAssignedDoctorName() + ".");
        }
        if (visit.getStatus() == VisitStatus.REGISTERED && visit.getPriority() != TriagePriority.EMERGENCY) {
            throw new BusinessRuleException("Vitals must be recorded by the nurse before consultation (except emergencies).");
        }

        String note = "Consultation started";
        if (visit.getAssignedDoctor() != null && !visit.getAssignedDoctor().equals(doctor.username())) {
            note = "Taken over from " + visit.getAssignedDoctorName();
        }
        visit.setAssignedDoctor(doctor.username());
        visit.setAssignedDoctorName(doctor.fullName());
        if (visit.getConsultationStartedAt() == null) {
            visit.setConsultationStartedAt(Instant.now(clock));
        }
        moveTo(visit, VisitStatus.IN_CONSULTATION, note);
        return detail(visit);
    }

    @Transactional
    public VisitDetailResponse saveConsultation(Long id, ConsultationNotesRequest request) {
        Visit visit = find(id);
        requireMyConsultation(visit);
        visit.setConsultationNotes(PatientService.blankToNull(request.consultationNotes()));
        visit.setDiagnosis(PatientService.blankToNull(request.diagnosis()));
        return detail(visit);
    }

    /**
     * Places the order in lab-service first, then moves the visit to AWAITING_LAB.
     * If lab-service is down nothing changes here and the doctor gets a 502 with a clear message.
     */
    @Transactional
    public VisitDetailResponse orderLab(Long id, LabOrderRequest request) {
        Visit visit = find(id);
        requireMyConsultation(visit);
        Patient p = visit.getPatient();
        LabOrderPayload payload = new LabOrderPayload(
                visit.getId(), p.getId(), p.getMrn(), p.getFullName(), p.getGender().name(),
                AgeCalculator.years(p.getDateOfBirth(), LocalDate.now(clock)),
                CurrentUser.get().fullName(),
                request.testCodes().stream().map(String::trim).map(String::toUpperCase).distinct().toList(),
                request.priority() != null ? request.priority() : "ROUTINE",
                PatientService.blankToNull(request.clinicalNotes()));

        LabOrderCreated created = labClient.createOrder(payload);
        visit.setLatestLabOrderId(created.id());
        moveTo(visit, VisitStatus.AWAITING_LAB, "Lab order " + created.orderNumber() + ": " + String.join(", ", payload.testCodes()));
        return detail(visit);
    }

    @Transactional
    public VisitDetailResponse complete(Long id) {
        Visit visit = find(id);
        requireMyConsultation(visit);
        if (visit.getDiagnosis() == null || visit.getDiagnosis().isBlank()) {
            throw new BusinessRuleException("Record a diagnosis before completing the visit.");
        }
        visit.setCompletedAt(Instant.now(clock));
        moveTo(visit, VisitStatus.COMPLETED, "Diagnosis: " + visit.getDiagnosis());
        return detail(visit);
    }

    // ---------------------------------------------------------------- lab callback

    /**
     * Called by lab-service when a report is released. Waiting visits move to LAB_RESULTS_READY;
     * otherwise the release is only logged. Critical results raise a NORMAL priority to URGENT.
     */
    @Transactional
    public void labResultsReady(Long id, LabResultsReadyRequest request) {
        // FIXED: Wrap in an optional check to prevent NotFoundException from crashing the caller microservice
        Optional<Visit> visitOpt = visits.findWithPatientById(id);
        
        if (visitOpt.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(VisitService.class).error(
                "⚠️ WEBHOOK ORPHAN DETECTED: Lab service notified about visit ID {} but no such visit exists in OPD database. Order Number: {}", 
                id, request.orderNumber()
            );
            return; // Exit safely without a 500 error or transaction rollback!
        }

        Visit visit = visitOpt.get();
        String label = request.orderNumber() != null ? request.orderNumber() : "#" + request.labOrderId();
        String note = "Lab results released for " + label + (request.hasCriticalResults() ? " (CRITICAL values)" : "");
        
        if (visit.getStatus().isActive() && request.hasCriticalResults()) {
            note = joinNotes(note, changePriority(visit, visit.getPriority() == TriagePriority.NORMAL
                    ? TriagePriority.URGENT : visit.getPriority()));
        }
        
        if (visit.getStatus() == VisitStatus.AWAITING_LAB) {
            moveTo(visit, VisitStatus.LAB_RESULTS_READY, note);
        } else {
            log(visit, visit.getStatus(), visit.getStatus(), note);
        }
    }

    // ---------------------------------------------------------------- helpers

    private Visit find(Long id) {
        return visits.findWithPatientById(id).orElseThrow(() -> new NotFoundException("Visit " + id + " not found."));
    }

    private void requireActive(Visit visit) {
        if (!visit.getStatus().isActive()) {
            throw new BusinessRuleException("This visit is " + visit.getStatus().name().toLowerCase() + " and can no longer be changed.");
        }
    }

    private void requireMyConsultation(Visit visit) {
        if (visit.getStatus() != VisitStatus.IN_CONSULTATION) {
            throw new BusinessRuleException("Start the consultation first (current status: " + visit.getStatus() + ").");
        }
        if (!CurrentUser.username().equals(visit.getAssignedDoctor())) {
            throw new BusinessRuleException("This patient is under " + visit.getAssignedDoctorName() + ".");
        }
    }

    private void moveTo(Visit visit, VisitStatus target, String note) {
        VisitStatus from = visit.getStatus();
        if (!from.canMoveTo(target)) {
            throw new BusinessRuleException("A visit cannot move from " + from + " to " + target + ".");
        }
        visit.setStatus(target);
        log(visit, from, target, note);
    }

    /** Returns an audit note when the priority actually changes, otherwise null. */
    private String changePriority(Visit visit, TriagePriority target) {
        if (target == null || target == visit.getPriority()) {
            return null;
        }
        String note = "Priority " + visit.getPriority() + " → " + target;
        visit.setPriority(target);
        return note;
    }

    private void log(Visit visit, VisitStatus from, VisitStatus to, String note) {
        events.save(new VisitEvent(visit, from, to, CurrentUser.username(), note, Instant.now(clock)));
    }

    private static String joinNotes(String a, String b) {
        return b == null ? a : a + ". " + b;
    }

    private VisitDetailResponse detail(Visit visit) {
        return mapper.toDetail(visit,
                vitalsRepo.findByVisitIdOrderByRecordedAtDesc(visit.getId()),
                events.findByVisitIdOrderByOccurredAtAsc(visit.getId()));
    }

    /** Maps a list of visits with their latest vitals using one extra query, not one per row. */
    private List<VisitSummaryResponse> summaries(List<Visit> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<Long, VitalSigns> latest = vitalsRepo.findByVisitIdInOrderByRecordedAtDesc(
                        list.stream().map(Visit::getId).toList()).stream()
                .collect(Collectors.toMap(v -> v.getVisit().getId(), Function.identity(), (first, older) -> first));
        return list.stream().map(v -> mapper.toSummary(v, latest.get(v.getId()))).toList();
    }
}
