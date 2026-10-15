package com.hospital.opd.service;

import com.hospital.opd.model.VitalFlag;
import com.hospital.opd.model.VitalSigns;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flags vital signs against adult limits.
 *
 * <p>Clinical review required: these thresholds are common adult defaults and must be confirmed by the
 * hospital's medical team before go-live. Children have different normal ranges; for patients under 12
 * only the critical limits are applied, so normal paediatric values are not shown as abnormal.</p>
 */
@Component
public class VitalsAssessor {

    /** low, high, criticalLow, criticalHigh (null = no limit). */
    private record Limits(Double low, Double high, Double criticalLow, Double criticalHigh) {
    }

    private static final Map<String, Limits> ADULT = Map.of(
            "temperatureC", new Limits(36.1, 37.5, 35.0, 40.0),
            "pulseBpm", new Limits(60.0, 100.0, 40.0, 140.0),
            "respiratoryRate", new Limits(12.0, 20.0, 8.0, 30.0),
            "systolicBp", new Limits(90.0, 139.0, 80.0, 180.0),
            "diastolicBp", new Limits(60.0, 89.0, 40.0, 120.0),
            "spo2", new Limits(95.0, null, 90.0, null)
    );

    public Map<String, VitalFlag> assess(VitalSigns v, int ageYears) {
        boolean criticalOnly = ageYears < 12;
        Map<String, VitalFlag> flags = new LinkedHashMap<>();
        put(flags, "temperatureC", v.getTemperatureC() == null ? null : v.getTemperatureC().doubleValue(), criticalOnly);
        put(flags, "pulseBpm", toDouble(v.getPulseBpm()), criticalOnly);
        put(flags, "respiratoryRate", toDouble(v.getRespiratoryRate()), criticalOnly);
        put(flags, "systolicBp", toDouble(v.getSystolicBp()), criticalOnly);
        put(flags, "diastolicBp", toDouble(v.getDiastolicBp()), criticalOnly);
        put(flags, "spo2", toDouble(v.getSpo2()), criticalOnly);
        return flags;
    }

    public static boolean anyCritical(Map<String, VitalFlag> flags) {
        return flags.values().stream().anyMatch(f -> f == VitalFlag.CRITICAL_LOW || f == VitalFlag.CRITICAL_HIGH);
    }

    /** BMI = kg / m², one decimal. Null unless both weight and height are recorded. */
    public static BigDecimal bmi(VitalSigns v) {
        if (v.getWeightKg() == null || v.getHeightCm() == null || v.getHeightCm().signum() == 0) {
            return null;
        }
        BigDecimal metres = v.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return v.getWeightKg().divide(metres.multiply(metres), 1, RoundingMode.HALF_UP);
    }

    private static void put(Map<String, VitalFlag> flags, String field, Double value, boolean criticalOnly) {
        if (value == null) {
            return;
        }
        Limits l = ADULT.get(field);
        VitalFlag flag;
        if (l.criticalLow() != null && value < l.criticalLow()) {
            flag = VitalFlag.CRITICAL_LOW;
        } else if (l.criticalHigh() != null && value > l.criticalHigh()) {
            flag = VitalFlag.CRITICAL_HIGH;
        } else if (!criticalOnly && l.low() != null && value < l.low()) {
            flag = VitalFlag.LOW;
        } else if (!criticalOnly && l.high() != null && value > l.high()) {
            flag = VitalFlag.HIGH;
        } else {
            flag = VitalFlag.NORMAL;
        }
        flags.put(field, flag);
    }

    private static Double toDouble(Integer value) {
        return value == null ? null : value.doubleValue();
    }
}
