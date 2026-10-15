package com.hospital.opd.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hospital.opd.model.TriagePriority;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Vitals recorded by the nurse (or doctor). Bounds reject impossible values (typos),
 * they are not clinical normal ranges; those live in VitalsAssessor.
 */
public record TriageRequest(
        @DecimalMin("25.0") @DecimalMax("45.0") BigDecimal temperatureC,
        @Min(20) @Max(250) Integer pulseBpm,
        @Min(4) @Max(80) Integer respiratoryRate,
        @Min(40) @Max(300) Integer systolicBp,
        @Min(20) @Max(200) Integer diastolicBp,
        @Min(50) @Max(100) Integer spo2,
        @DecimalMin("0.5") @DecimalMax("400.0") BigDecimal weightKg,
        @DecimalMin("30.0") @DecimalMax("250.0") BigDecimal heightCm,
        TriagePriority priority) {

    @JsonIgnore
    @AssertTrue(message = "Record at least one vital sign")
    public boolean isAnyVitalProvided() {
        return temperatureC != null || pulseBpm != null || respiratoryRate != null || systolicBp != null
                || diastolicBp != null || spo2 != null || weightKg != null || heightCm != null;
    }

    @JsonIgnore
    @AssertTrue(message = "Enter both systolic and diastolic pressure, with systolic higher than diastolic")
    public boolean isBloodPressureConsistent() {
        if (systolicBp == null && diastolicBp == null) {
            return true;
        }
        return systolicBp != null && diastolicBp != null && systolicBp > diastolicBp;
    }
}
