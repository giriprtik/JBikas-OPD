package com.hospital.opd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Doctor's lab request. Test codes come from GET /api/lab/catalog. */
public record LabOrderRequest(
        @NotEmpty @Size(max = 20) List<@NotBlank String> testCodes,
        @Pattern(regexp = "ROUTINE|URGENT|STAT", message = "must be ROUTINE, URGENT or STAT") String priority,
        @Size(max = 1000) String clinicalNotes) {
}
