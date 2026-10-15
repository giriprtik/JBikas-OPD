package com.hospital.opd.service;

import com.hospital.opd.dto.PatientRequest;
import com.hospital.opd.dto.PatientResponse;
import com.hospital.opd.dto.VisitSummaryResponse;
import com.hospital.opd.exception.BusinessRuleException;
import com.hospital.opd.exception.NotFoundException;
import com.hospital.opd.model.Patient;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.repository.VisitRepository;
import com.hospital.opd.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PatientService {

    private static final Pattern MRN = Pattern.compile("(?i)^JB-\\d{1,6}$");
    private static final Pattern DIGITS = Pattern.compile("^\\+?\\d{3,15}$");

    private final PatientRepository patients;
    private final VisitRepository visits;
    private final SequenceService sequences;
    private final OpdMapper mapper;
    private final Clock clock;

    public PatientService(PatientRepository patients, VisitRepository visits, SequenceService sequences,
                          OpdMapper mapper, Clock clock) {
        this.patients = patients;
        this.visits = visits;
        this.sequences = sequences;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional
    public PatientResponse register(PatientRequest request) {
        String name = normaliseName(request.fullName());
        String phone = request.phone().trim();
        Optional<Patient> duplicate = patients.findTop20ByPhoneContainingOrderByFullNameAsc(phone).stream()
                .filter(p -> p.getPhone().equals(phone) && p.getFullName().equalsIgnoreCase(name))
                .findFirst();
        if (duplicate.isPresent()) {
            throw new BusinessRuleException("This patient is already registered as " + duplicate.get().getMrn()
                    + ". Search for the existing record instead of creating a new one.");
        }
        Patient patient = new Patient();
        patient.setMrn(sequences.nextMrn());
        patient.setCreatedAt(Instant.now(clock));
        patient.setCreatedBy(CurrentUser.username());
        apply(patient, request, name, phone);
        return mapper.toResponse(patients.save(patient));
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = find(id);
        apply(patient, request, normaliseName(request.fullName()), request.phone().trim());
        patient.setUpdatedAt(Instant.now(clock));
        return mapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public PatientResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    /** MRN (exact), phone (partial) or name (partial, case-insensitive). At most 20 matches. */
    @Transactional(readOnly = true)
    public List<PatientResponse> search(String query) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            throw new IllegalArgumentException("Type at least 2 characters to search.");
        }
        List<Patient> found;
        if (MRN.matcher(q).matches()) {
            found = patients.findByMrnIgnoreCase(q).map(List::of).orElse(List.of());
        } else if (DIGITS.matcher(q).matches()) {
            found = patients.findTop20ByPhoneContainingOrderByFullNameAsc(q);
        } else {
            found = patients.findTop20ByFullNameContainingIgnoreCaseOrderByFullNameAsc(q);
        }
        return found.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VisitSummaryResponse> history(Long patientId) {
        find(patientId);
        return visits.findByPatientIdOrderByRegisteredAtDesc(patientId).stream()
                .map(v -> mapper.toSummary(v, null))
                .toList();
    }

    Patient find(Long id) {
        return patients.findById(id).orElseThrow(() -> new NotFoundException("Patient " + id + " not found."));
    }

    private void apply(Patient patient, PatientRequest r, String name, String phone) {
        LocalDate today = LocalDate.now(clock);
        if (r.dateOfBirth() != null) {
            patient.setDateOfBirth(r.dateOfBirth());
            patient.setDobEstimated(false);
        } else {
            patient.setDateOfBirth(LocalDate.of(today.getYear() - r.ageYears(), 1, 1));
            patient.setDobEstimated(true);
        }
        patient.setFullName(name);
        patient.setGender(r.gender());
        patient.setPhone(phone);
        patient.setAddress(blankToNull(r.address()));
        patient.setBloodGroup(blankToNull(r.bloodGroup()));
        patient.setAllergies(blankToNull(r.allergies()));
        patient.setEmergencyContactName(blankToNull(r.emergencyContactName()));
        patient.setEmergencyContactPhone(blankToNull(r.emergencyContactPhone()));
    }

    private static String normaliseName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
