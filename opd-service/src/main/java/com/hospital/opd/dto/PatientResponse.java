package com.hospital.opd.dto;

import com.hospital.opd.model.Gender;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String mrn,
        String fullName,
        LocalDate dateOfBirth,
        boolean dobEstimated,
        int ageYears,
        String ageDisplay,
        Gender gender,
        String phone,
        String address,
        String bloodGroup,
        String allergies,
        String emergencyContactName,
        String emergencyContactPhone,
        Instant createdAt) {
}
