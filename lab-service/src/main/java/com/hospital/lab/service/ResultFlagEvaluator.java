package com.hospital.lab.service;

import com.hospital.lab.model.ResultFlag;
import com.hospital.lab.model.ValueType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Turns a result value into a flag. This is the single place where "high", "low" and "critical"
 * are decided; the frontend only previews the same rules.
 *
 * <ul>
 *   <li>NUMERIC: below critical low → CRITICAL_LOW; above critical high → CRITICAL_HIGH; below low → LOW;
 *       above high → HIGH; otherwise NORMAL. Limits are inclusive (a value equal to the limit is normal).
 *       No limits at all → NONE.</li>
 *   <li>TEXT: matches one of the normal values (case-insensitive) → NORMAL, otherwise ABNORMAL.
 *       No normal values defined → NONE.</li>
 *   <li>NARRATIVE: always NONE.</li>
 * </ul>
 * Returns null when there is no value yet.
 */
public final class ResultFlagEvaluator {

    private ResultFlagEvaluator() {
    }

    public static ResultFlag evaluate(ValueType type, BigDecimal numericValue, String textValue,
                                      ResolvedRange range, List<String> normalValues) {
        return switch (type) {
            case NUMERIC -> numeric(numericValue, range);
            case TEXT -> text(textValue, normalValues);
            case NARRATIVE -> textValue == null || textValue.isBlank() ? null : ResultFlag.NONE;
        };
    }

    static ResultFlag numeric(BigDecimal value, ResolvedRange r) {
        if (value == null) {
            return null;
        }
        if (r == null || (r.low() == null && r.high() == null && r.criticalLow() == null && r.criticalHigh() == null)) {
            return ResultFlag.NONE;
        }
        if (r.criticalLow() != null && value.compareTo(r.criticalLow()) < 0) {
            return ResultFlag.CRITICAL_LOW;
        }
        if (r.criticalHigh() != null && value.compareTo(r.criticalHigh()) > 0) {
            return ResultFlag.CRITICAL_HIGH;
        }
        if (r.low() != null && value.compareTo(r.low()) < 0) {
            return ResultFlag.LOW;
        }
        if (r.high() != null && value.compareTo(r.high()) > 0) {
            return ResultFlag.HIGH;
        }
        return ResultFlag.NORMAL;
    }

    static ResultFlag text(String value, List<String> normalValues) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (normalValues == null || normalValues.isEmpty()) {
            return ResultFlag.NONE;
        }
        String v = value.trim();
        return normalValues.stream().anyMatch(n -> n.equalsIgnoreCase(v)) ? ResultFlag.NORMAL : ResultFlag.ABNORMAL;
    }
}
