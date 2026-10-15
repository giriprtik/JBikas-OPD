package com.hospital.lab.dto;

import com.hospital.lab.model.ResultFlag;
import com.hospital.lab.model.ValueType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * {@code referenceText} is ready to print ("70 – 100", "≤ 200", "Nil / Negative").
 * The numeric limits are included so the UI can preview flags while the technician types.
 */
public record LabResultResponse(
        Long id,
        String parameterCode,
        String parameterName,
        ValueType valueType,
        String unit,
        int decimals,
        String value,
        ResultFlag flag,
        String referenceText,
        BigDecimal refLow,
        BigDecimal refHigh,
        BigDecimal criticalLow,
        BigDecimal criticalHigh,
        List<String> normalValues,
        String enteredBy,
        Instant enteredAt,
        List<AmendmentResponse> amendments) {

    public record AmendmentResponse(String previousValue, String newValue, ResultFlag previousFlag, ResultFlag newFlag,
                                    String reason, String amendedBy, Instant amendedAt) {
    }
}
