package com.hospital.opd.controller;

import com.hospital.opd.dto.*;
import com.hospital.opd.model.VisitStatus;
import com.hospital.opd.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/opd/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    /** Day list. Defaults to today in the hospital time zone; filter with ?status=REGISTERED&status=TRIAGED. */
    @GetMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public List<VisitSummaryResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Set<VisitStatus> status) {
        return visitService.listForDay(date, status);
    }

    @GetMapping("/doctor-queue")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<VisitSummaryResponse> doctorQueue() {
        return visitService.doctorQueue();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR', 'LAB_TECH')")
    public VisitDetailResponse get(@PathVariable Long id) {
        return visitService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<VisitDetailResponse> open(@Valid @RequestBody VisitCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.open(request));
    }

    @PostMapping("/{id}/vitals")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public VisitDetailResponse recordVitals(@PathVariable Long id, @Valid @RequestBody TriageRequest request) {
        return visitService.recordVitals(id, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public VisitDetailResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        return visitService.cancel(id, request);
    }

    @PostMapping("/{id}/start-consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitDetailResponse startConsultation(@PathVariable Long id) {
        return visitService.startConsultation(id);
    }

    @PutMapping("/{id}/consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitDetailResponse saveConsultation(@PathVariable Long id, @Valid @RequestBody ConsultationNotesRequest request) {
        return visitService.saveConsultation(id, request);
    }

    @PostMapping("/{id}/lab-orders")
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitDetailResponse orderLab(@PathVariable Long id, @Valid @RequestBody LabOrderRequest request) {
        return visitService.orderLab(id, request);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public VisitDetailResponse complete(@PathVariable Long id) {
        return visitService.complete(id);
    }

    /** Callback from lab-service when a report is released (lab technician's token is forwarded). */
    @PostMapping("/{id}/lab-results-ready")
    @PreAuthorize("hasRole('LAB_TECH') or hasRole('SYSTEM')")
    public ResponseEntity<Void> labResultsReady(@PathVariable Long id, @Valid @RequestBody LabResultsReadyRequest request) {
        visitService.labResultsReady(id, request);
        return ResponseEntity.noContent().build();
    }
}
