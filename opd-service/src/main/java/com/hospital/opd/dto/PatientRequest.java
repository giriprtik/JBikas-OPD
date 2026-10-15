package com.hospital.opd.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hospital.opd.model.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Registration / update payload. Give either {@code dateOfBirth} or {@code ageYears};
 * with age only, the DOB is estimated as 1 January of the birth year and flagged as estimated.
 */
public record PatientRequest(
        @NotBlank @Size(max = 150) String fullName,
        @PastOrPresent LocalDate dateOfBirth,
        @Min(0) @Max(130) Integer ageYears,
        @NotNull Gender gender,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7–15 digits, optionally starting with +") String phone,
        @Size(max = 255) String address,
        @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "must be one of A+, A-, B+, B-, AB+, AB-, O+, O-") String bloodGroup,
        @Size(max = 1000) String allergies,
        @Size(max = 150) String emergencyContactName,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7–15 digits, optionally starting with +") String emergencyContactPhone) {

    @JsonIgnore
    @AssertTrue(message = "Provide either date of birth or age")
    public boolean isDateOfBirthOrAgeProvided() {
        return dateOfBirth != null || ageYears != null;
    }
}
