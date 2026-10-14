package com.hospital.opd.controller;

import com.hospital.opd.model.Patient;
import com.hospital.opd.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/opd/patients")
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // 🩺 Both Doctors and Nurses can see the active patient queue list
    @GetMapping("/queue")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<Patient>> getActiveQueue() {
        return ResponseEntity.ok(patientRepository.findByStatus("QUEUED"));
    }

    // 📝 ONLY Nurses are authorized to register incoming clinic patients
    @PostMapping("/register")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        Patient savedPatient = patientRepository.save(new Patient(
            patient.getName(),
            patient.getAge(),
            patient.getGender(),
            patient.getContactNumber(),
            patient.getChiefComplaint()
        ));
        return ResponseEntity.ok(savedPatient);
    }

    // 📋 Allows both Doctors and Nurses to view the historical master list of all patients
    @GetMapping("/all")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<List<Patient>> getAllRegisteredPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

}
