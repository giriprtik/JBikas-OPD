package com.hospital.lab.dto;

import com.hospital.lab.model.LabOrderStatus;
import com.hospital.lab.model.OrderPriority;

import java.time.Instant;
import java.util.List;

/**
 * Full order. For doctors, result values are only included once the order is RELEASED
 * ({@code resultsVisible}); before that they see progress but no numbers.
 */
public record LabOrderResponse(
        Long id,
        String orderNumber,
        Long visitId,
        Long patientId,
        String mrn,
        String patientName,
        String patientGender,
        int patientAgeYears,
        String orderingDoctor,
        String orderingDoctorName,
        OrderPriority priority,
        String clinicalNotes,
        LabOrderStatus status,
        Instant orderedAt,
        Instant sampleCollectedAt,
        String sampleCollectedBy,
        Instant releasedAt,
        String releasedByName,
        String criticalNotifiedTo,
        String cancelReason,
        boolean amended,
        boolean resultsVisible,
        int abnormalCount,
        int criticalCount,
        List<LabItemResponse> items,
        List<AttachmentResponse> attachments) {
}
