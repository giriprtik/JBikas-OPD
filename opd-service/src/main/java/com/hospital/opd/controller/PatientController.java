package com.hospital.opd.controller;

import com.hospital.opd.dto.PatientRequest;
import com.hospital.opd.dto.PatientResponse;
import com.hospital.opd.dto.VisitSummaryResponse;
import com.hospital.opd.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opd/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** Front desk registration of a new patient (no visit is opened yet). */
    @PostMapping
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.register(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NURSE')")
    public PatientResponse update(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return patientService.update(id, request);
    }

    /** ?q= MRN (JB-000123), phone digits, or part of the name. */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public List<PatientResponse> search(@RequestParam("q") String query) {
        return patientService.search(query);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public PatientResponse get(@PathVariable Long id) {
        return patientService.get(id);
    }

    /** Previous visits, newest first. */
    @GetMapping("/{id}/visits")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public List<VisitSummaryResponse> history(@PathVariable Long id) {
        return patientService.history(id);
    }
}
