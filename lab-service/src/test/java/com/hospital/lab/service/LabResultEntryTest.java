package com.hospital.lab.service;

import com.hospital.lab.model.LabResult;
import com.hospital.lab.model.ResultFlag;
import com.hospital.lab.model.ValueType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Typed values → stored value + flag, as done when the technician saves results. */
class LabResultEntryTest {

    @Test
    void numericEntryIsParsedAndFlagged() {
        LabResult potassium = numericResult("Potassium", "3.5", "5.1", "2.5", "6.5");
        LabOrderService.applyValue(potassium, " 6.9 ");
        assertEquals(new BigDecimal("6.9"), potassium.getNumericValue());
        assertEquals(ResultFlag.CRITICAL_HIGH, potassium.getFlag());
        assertEquals("6.9", potassium.displayValue());
    }

    @Test
    void blankClearsTheValueAndFlag() {
        LabResult r = numericResult("Sodium", "135", "145", "120", "160");
        LabOrderService.applyValue(r, "140");
        LabOrderService.applyValue(r, "");
        assertNull(r.getNumericValue());
        assertNull(r.getFlag());
    }

    @Test
    void nonNumbersAreRejectedWithTheParameterName() {
        LabResult r = numericResult("Sodium", "135", "145", null, null);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> LabOrderService.applyValue(r, "13o"));
        assertTrue(e.getMessage().contains("Sodium"));
    }

    @Test
    void textEntryUsesTheNormalList() {
        LabResult protein = new LabResult();
        protein.setParameterName("Protein");
        protein.setValueType(ValueType.TEXT);
        protein.setNormalValues("Nil,Negative");
        LabOrderService.applyValue(protein, "1+");
        assertEquals(ResultFlag.ABNORMAL, protein.getFlag());
    }

    @Test
    void displayValueUsesTheParameterDecimals() {
        LabResult r = numericResult("Haemoglobin", "13.5", "17.5", "7", "20");
        r.setDecimals(1);
        LabOrderService.applyValue(r, "9");
        assertEquals("9.0", r.displayValue());
        assertEquals(ResultFlag.LOW, r.getFlag());
    }

    private static LabResult numericResult(String name, String low, String high, String critLow, String critHigh) {
        LabResult r = new LabResult();
        r.setParameterName(name);
        r.setValueType(ValueType.NUMERIC);
        r.setDecimals(1);
        r.setRefLow(new BigDecimal(low));
        r.setRefHigh(new BigDecimal(high));
        r.setCriticalLow(critLow == null ? null : new BigDecimal(critLow));
        r.setCriticalHigh(critHigh == null ? null : new BigDecimal(critHigh));
        return r;
    }
}
