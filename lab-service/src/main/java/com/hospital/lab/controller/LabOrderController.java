package com.hospital.lab.controller;

import com.hospital.lab.dto.*;
import com.hospital.lab.model.LabOrderStatus;
import com.hospital.lab.security.CurrentUser;
import com.hospital.lab.service.AttachmentService;
import com.hospital.lab.service.LabOrderService;
import com.hospital.lab.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/lab/orders")
public class LabOrderController {

    private final LabOrderService orderService;
    private final AttachmentService attachmentService;
    private final ReportService reportService;

    public LabOrderController(LabOrderService orderService, AttachmentService attachmentService, ReportService reportService) {
        this.orderService = orderService;
        this.attachmentService = attachmentService;
        this.reportService = reportService;
    }

    // ---------------------------------------------------------------- orders

    /** Called by opd-service with the doctor's token. */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabOrderResponse> create(@Valid @RequestBody CreateLabOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    /** Worklist. Default: open orders. ?status=RELEASED shows the last 24 h. */
    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public List<LabOrderSummary> worklist(@RequestParam(required = false) Set<LabOrderStatus> status) {
        return orderService.worklist(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public LabOrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @GetMapping("/by-visit/{visitId}")
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR', 'NURSE')")
    public List<LabOrderResponse> forVisit(@PathVariable Long visitId) {
        return orderService.forVisit(visitId);
    }

    /** Released reports of a patient across visits (for trends and previous results). */
    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public List<LabOrderResponse> forPatient(@PathVariable Long patientId) {
        return orderService.forPatient(patientId);
    }

    @PostMapping("/{id}/collect-sample")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabOrderResponse collectSample(@PathVariable Long id) {
        return orderService.collectSample(id);
    }

    @PutMapping("/{id}/results")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabOrderResponse saveResults(@PathVariable Long id, @Valid @RequestBody ResultEntryRequest request) {
        return orderService.saveResults(id, request);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabOrderResponse release(@PathVariable Long id, @Valid @RequestBody(required = false) ReleaseRequest request) {
        return orderService.release(id, request);
    }

    @PostMapping("/{id}/results/{resultId}/amend")
    @PreAuthorize("hasRole('LAB_TECH')")
    public LabOrderResponse amend(@PathVariable Long id, @PathVariable Long resultId, @Valid @RequestBody AmendRequest request) {
        return orderService.amend(id, resultId, request);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public LabOrderResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        return orderService.cancel(id, request);
    }

    // ---------------------------------------------------------------- report

    /** PDF report. Doctors get released reports only; lab staff can preview a watermarked draft. */
    @GetMapping(value = "/{id}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public ResponseEntity<byte[]> report(@PathVariable Long id) {
        ReportService.Report report = reportService.render(id, CurrentUser.hasRole("LAB_TECH"));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(report.filename()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(report.pdf());
    }

    // ---------------------------------------------------------------- attachments

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('LAB_TECH')")
    public ResponseEntity<AttachmentResponse> upload(@PathVariable Long id,
                                                     @RequestPart("file") MultipartFile file,
                                                     @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.upload(id, file, description));
    }

    @GetMapping("/{id}/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('LAB_TECH', 'DOCTOR')")
    public ResponseEntity<Resource> download(@PathVariable Long id, @PathVariable Long attachmentId) {
        AttachmentService.StoredFile file = attachmentService.download(id, attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @PreAuthorize("hasRole('LAB_TECH')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        attachmentService.delete(id, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
