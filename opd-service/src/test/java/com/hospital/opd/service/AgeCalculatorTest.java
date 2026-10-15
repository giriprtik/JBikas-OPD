package com.hospital.opd.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgeCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);

    @Test
    void displaysAgeTheWayCliniciansWriteIt() {
        assertEquals("34 y", AgeCalculator.display(LocalDate.of(1992, 3, 1), TODAY));
        assertEquals("18 mo", AgeCalculator.display(LocalDate.of(2025, 4, 10), TODAY));
        assertEquals("12 d", AgeCalculator.display(LocalDate.of(2026, 10, 2), TODAY));
    }

    @Test
    void yearsBeforeBirthdayAreNotRoundedUp() {
        assertEquals(33, AgeCalculator.years(LocalDate.of(1992, 12, 1), TODAY));
        assertEquals(34, AgeCalculator.years(LocalDate.of(1992, 10, 14), TODAY));
    }
}
