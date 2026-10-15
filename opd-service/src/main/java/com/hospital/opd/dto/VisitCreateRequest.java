package com.hospital.opd.dto;

import com.hospital.opd.model.TriagePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Opens a visit for an already registered patient. Priority defaults to NORMAL until triage. */
public record VisitCreateRequest(
        @NotNull Long patientId,
        @NotBlank @Size(max = 2000) String chiefComplaint,
        TriagePriority priority) {
}
