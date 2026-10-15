package com.hospital.opd.service;

import com.hospital.opd.dto.*;
import com.hospital.opd.model.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Entity → response conversion. Keeps controllers and services free of mapping noise. */
@Component
public class OpdMapper {

    private final Clock clock;
    private final VitalsAssessor vitalsAssessor;

    public OpdMapper(Clock clock, VitalsAssessor vitalsAssessor) {
        this.clock = clock;
        this.vitalsAssessor = vitalsAssessor;
    }

    public PatientResponse toResponse(Patient p) {
        LocalDate today = LocalDate.now(clock);
        return new PatientResponse(p.getId(), p.getMrn(), p.getFullName(), p.getDateOfBirth(), p.isDobEstimated(),
                AgeCalculator.years(p.getDateOfBirth(), today), AgeCalculator.display(p.getDateOfBirth(), today),
                p.getGender(), p.getPhone(), p.getAddress(), p.getBloodGroup(), p.getAllergies(),
                p.getEmergencyContactName(), p.getEmergencyContactPhone(), p.getCreatedAt());
    }

    public PatientBrief toBrief(Patient p) {
        LocalDate today = LocalDate.now(clock);
        return new PatientBrief(p.getId(), p.getMrn(), p.getFullName(), AgeCalculator.years(p.getDateOfBirth(), today),
                AgeCalculator.display(p.getDateOfBirth(), today), p.getGender(), p.getPhone(), p.getAllergies());
    }

    public VitalsResponse toResponse(VitalSigns v, int ageYears) {
        Map<String, VitalFlag> flags = vitalsAssessor.assess(v, ageYears);
        return new VitalsResponse(v.getId(), v.getTemperatureC(), v.getPulseBpm(), v.getRespiratoryRate(),
                v.getSystolicBp(), v.getDiastolicBp(), v.getSpo2(), v.getWeightKg(), v.getHeightCm(),
                VitalsAssessor.bmi(v), flags, VitalsAssessor.anyCritical(flags), v.getRecordedBy(), v.getRecordedAt());
    }

    public VisitSummaryResponse toSummary(Visit visit, VitalSigns latestVitals) {
        PatientBrief patient = toBrief(visit.getPatient());
        return new VisitSummaryResponse(visit.getId(), visit.getVisitDate(), visit.getTokenNumber(), visit.getStatus(),
                visit.getPriority(), visit.getChiefComplaint(), visit.getAssignedDoctor(), visit.getAssignedDoctorName(),
                visit.getLatestLabOrderId(), visit.getRegisteredAt(), visit.getConsultationStartedAt(), patient,
                latestVitals == null ? null : toResponse(latestVitals, patient.ageYears()));
    }

    public VisitDetailResponse toDetail(Visit visit, List<VitalSigns> vitals, List<VisitEvent> events) {
        PatientResponse patient = toResponse(visit.getPatient());
        return new VisitDetailResponse(visit.getId(), visit.getVisitDate(), visit.getTokenNumber(), visit.getStatus(),
                visit.getPriority(), visit.getChiefComplaint(), visit.getAssignedDoctor(), visit.getAssignedDoctorName(),
                visit.getConsultationNotes(), visit.getDiagnosis(), visit.getLatestLabOrderId(), visit.getCancelReason(),
                visit.getRegisteredAt(), visit.getRegisteredBy(), visit.getConsultationStartedAt(), visit.getCompletedAt(),
                patient,
                vitals.stream().map(v -> toResponse(v, patient.ageYears())).toList(),
                events.stream().map(e -> new VisitEventResponse(e.getFromStatus(), e.getToStatus(), e.getActor(),
                        e.getNote(), e.getOccurredAt())).toList());
    }
}
