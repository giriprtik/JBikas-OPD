package com.hospital.opd.dto;

import com.hospital.opd.model.Gender;

/** Patient fields needed on queue rows. */
public record PatientBrief(Long id, String mrn, String fullName, int ageYears, String ageDisplay, Gender gender,
                           String phone, String allergies) {
}
