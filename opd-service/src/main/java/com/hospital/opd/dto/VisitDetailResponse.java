package com.hospital.opd.dto;

import com.hospital.opd.model.TriagePriority;
import com.hospital.opd.model.VisitStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Everything the consultation screen needs for one visit. */
public record VisitDetailResponse(
        Long id,
        LocalDate visitDate,
        int tokenNumber,
        VisitStatus status,
        TriagePriority priority,
        String chiefComplaint,
        String assignedDoctor,
        String assignedDoctorName,
        String consultationNotes,
        String diagnosis,
        Long latestLabOrderId,
        String cancelReason,
        Instant registeredAt,
        String registeredBy,
        Instant consultationStartedAt,
        Instant completedAt,
        PatientResponse patient,
        List<VitalsResponse> vitals,
        List<VisitEventResponse> events) {
}
