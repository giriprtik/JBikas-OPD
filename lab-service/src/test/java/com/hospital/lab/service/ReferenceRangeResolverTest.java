package com.hospital.lab.service;

import com.hospital.lab.model.ReferenceRange;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceRangeResolverTest {

    /** Haemoglobin as seeded: child 1–12 any sex, adult male, adult female. */
    private static final List<ReferenceRange> HAEMOGLOBIN = List.of(
            range("ANY", 1, 12, "11.5", "15.5"),
            range("MALE", 12, null, "13.5", "17.5"),
            range("FEMALE", 12, null, "12.0", "15.5"));

    @Test
    void adultsGetTheirOwnSexRange() {
        assertEquals(new BigDecimal("13.5"), low(HAEMOGLOBIN, "MALE", 40));
        assertEquals(new BigDecimal("12.0"), low(HAEMOGLOBIN, "FEMALE", 40));
    }

    @Test
    void childrenGetThePaediatricRange() {
        assertEquals(new BigDecimal("11.5"), low(HAEMOGLOBIN, "MALE", 6));
        assertEquals(new BigDecimal("11.5"), low(HAEMOGLOBIN, "FEMALE", 11));
    }

    @Test
    void ageBandUpperLimitIsExclusive() {
        assertEquals(new BigDecimal("13.5"), low(HAEMOGLOBIN, "MALE", 12), "a 12-year-old falls in the 12+ band");
    }

    @Test
    void noBandForInfantsMeansNoRange() {
        assertTrue(ReferenceRangeResolver.resolve(HAEMOGLOBIN, "MALE", 0).isEmpty());
    }

    @Test
    void genderOtherUsesTheUnionOfMaleAndFemaleRanges() {
        Optional<ResolvedRange> r = ReferenceRangeResolver.resolve(HAEMOGLOBIN, "OTHER", 30);
        assertTrue(r.isPresent());
        assertEquals(new BigDecimal("12.0"), r.get().low());
        assertEquals(new BigDecimal("17.5"), r.get().high());
    }

    @Test
    void ownSexWinsOverAnyAndNarrowerBandWins() {
        List<ReferenceRange> ranges = List.of(
                range("ANY", 0, null, "1", "10"),
                range("ANY", 18, 60, "2", "9"),
                range("FEMALE", 0, null, "3", "8"));
        assertEquals(new BigDecimal("3"), low(ranges, "FEMALE", 30));
        assertEquals(new BigDecimal("2"), low(ranges, "MALE", 30));
        assertEquals(new BigDecimal("1"), low(ranges, "MALE", 70));
    }

    private static BigDecimal low(List<ReferenceRange> ranges, String gender, int age) {
        return ReferenceRangeResolver.resolve(ranges, gender, age).orElseThrow().low();
    }

    private static ReferenceRange range(String gender, int min, Integer max, String low, String high) {
        return new ReferenceRange(gender, min, max, new BigDecimal(low), new BigDecimal(high), null, null);
    }
}
