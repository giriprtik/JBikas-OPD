package com.hospital.opd.dto;

import com.hospital.opd.model.TriagePriority;
import com.hospital.opd.model.VisitStatus;

import java.time.Instant;
import java.time.LocalDate;

/** One row in a queue or day list. */
public record VisitSummaryResponse(
        Long id,
        LocalDate visitDate,
        int tokenNumber,
        VisitStatus status,
        TriagePriority priority,
        String chiefComplaint,
        String assignedDoctor,
        String assignedDoctorName,
        Long latestLabOrderId,
        Instant registeredAt,
        Instant consultationStartedAt,
        PatientBrief patient,
        VitalsResponse latestVitals) {
}
