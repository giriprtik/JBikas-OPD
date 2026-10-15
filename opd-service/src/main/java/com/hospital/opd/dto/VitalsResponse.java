package com.hospital.opd.dto;

import com.hospital.opd.model.VitalFlag;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** {@code flags} is keyed by field name (e.g. "spo2") and only contains recorded values. */
public record VitalsResponse(
        Long id,
        BigDecimal temperatureC,
        Integer pulseBpm,
        Integer respiratoryRate,
        Integer systolicBp,
        Integer diastolicBp,
        Integer spo2,
        BigDecimal weightKg,
        BigDecimal heightCm,
        BigDecimal bmi,
        Map<String, VitalFlag> flags,
        boolean hasCritical,
        String recordedBy,
        Instant recordedAt) {
}
