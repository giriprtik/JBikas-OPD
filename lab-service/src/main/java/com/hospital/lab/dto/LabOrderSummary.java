package com.hospital.lab.dto;

import com.hospital.lab.model.LabOrderStatus;
import com.hospital.lab.model.OrderPriority;

import java.time.Instant;
import java.util.List;

/** Worklist row. */
public record LabOrderSummary(
        Long id,
        String orderNumber,
        Long visitId,
        String mrn,
        String patientName,
        String patientGender,
        int patientAgeYears,
        String orderingDoctorName,
        OrderPriority priority,
        LabOrderStatus status,
        Instant orderedAt,
        Instant releasedAt,
        List<String> testNames,
        int resultsEntered,
        int resultsTotal,
        int criticalCount) {
}
