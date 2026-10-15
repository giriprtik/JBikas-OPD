package com.hospital.lab.dto;

import com.hospital.lab.model.OrderPriority;
import jakarta.validation.constraints.*;

import java.util.List;

/** Sent by opd-service on behalf of the doctor. Ordering doctor is taken from the token, not the body. */
public record CreateLabOrderRequest(
        @NotNull Long visitId,
        @NotNull Long patientId,
        @NotBlank @Size(max = 20) String mrn,
        @NotBlank @Size(max = 150) String patientName,
        @NotBlank @Pattern(regexp = "MALE|FEMALE|OTHER") String patientGender,
        @Min(0) @Max(130) int patientAgeYears,
        @Size(max = 150) String orderingDoctorName,
        @NotEmpty @Size(max = 20) List<@NotBlank String> testCodes,
        OrderPriority priority,
        @Size(max = 1000) String clinicalNotes) {
}
