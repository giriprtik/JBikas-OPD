package com.hospital.opd.service;

import com.hospital.opd.model.VitalFlag;
import com.hospital.opd.model.VitalSigns;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VitalsAssessorTest {

    private final VitalsAssessor assessor = new VitalsAssessor();

    @Test
    void adultVitalsAreFlagged() {
        VitalSigns v = new VitalSigns();
        v.setTemperatureC(new BigDecimal("38.6"));
        v.setPulseBpm(72);
        v.setSystolicBp(185);
        v.setDiastolicBp(95);
        v.setSpo2(88);
        Map<String, VitalFlag> flags = assessor.assess(v, 45);
        assertEquals(VitalFlag.HIGH, flags.get("temperatureC"));
        assertEquals(VitalFlag.NORMAL, flags.get("pulseBpm"));
        assertEquals(VitalFlag.CRITICAL_HIGH, flags.get("systolicBp"));
        assertEquals(VitalFlag.HIGH, flags.get("diastolicBp"));
        assertEquals(VitalFlag.CRITICAL_LOW, flags.get("spo2"));
        assertFalse(flags.containsKey("respiratoryRate"), "unrecorded vitals are not flagged");
        assertTrue(VitalsAssessor.anyCritical(flags));
    }

    @Test
    void childrenOnlyGetCriticalFlags() {
        VitalSigns v = new VitalSigns();
        v.setPulseBpm(120);       // normal for a 4-year-old, high for an adult
        v.setRespiratoryRate(26); // same
        Map<String, VitalFlag> flags = assessor.assess(v, 4);
        assertEquals(VitalFlag.NORMAL, flags.get("pulseBpm"));
        assertEquals(VitalFlag.NORMAL, flags.get("respiratoryRate"));
    }

    @Test
    void bmiNeedsWeightAndHeight() {
        VitalSigns v = new VitalSigns();
        v.setWeightKg(new BigDecimal("70.0"));
        assertNull(VitalsAssessor.bmi(v));
        v.setHeightCm(new BigDecimal("175.0"));
        assertEquals(new BigDecimal("22.9"), VitalsAssessor.bmi(v));
    }
}
