package com.hospital.lab.service;

import com.hospital.lab.model.ResultFlag;
import com.hospital.lab.model.ValueType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultFlagEvaluatorTest {

    /** Fasting glucose: normal 70–100 mg/dL, critical below 50 or above 400. */
    private static final ResolvedRange GLUCOSE = new ResolvedRange(bd("70"), bd("100"), bd("50"), bd("400"));

    @Test
    void fastingGlucoseIsFlaggedAgainstNormalAndCriticalLimits() {
        assertEquals(ResultFlag.NORMAL, numeric("85", GLUCOSE));
        assertEquals(ResultFlag.HIGH, numeric("126", GLUCOSE));
        assertEquals(ResultFlag.LOW, numeric("62", GLUCOSE));
        assertEquals(ResultFlag.CRITICAL_HIGH, numeric("450", GLUCOSE));
        assertEquals(ResultFlag.CRITICAL_LOW, numeric("42", GLUCOSE));
    }

    @Test
    void limitsAreInclusive() {
        assertEquals(ResultFlag.NORMAL, numeric("70", GLUCOSE));
        assertEquals(ResultFlag.NORMAL, numeric("100", GLUCOSE));
        assertEquals(ResultFlag.LOW, numeric("50", GLUCOSE), "equal to the critical limit is low, not critical");
        assertEquals(ResultFlag.HIGH, numeric("400", GLUCOSE));
    }

    @Test
    void oneSidedRangesOnlyFlagThatSide() {
        ResolvedRange hdlMale = new ResolvedRange(bd("40"), null, null, null);
        assertEquals(ResultFlag.LOW, numeric("35", hdlMale));
        assertEquals(ResultFlag.NORMAL, numeric("90", hdlMale));

        ResolvedRange ldl = new ResolvedRange(null, bd("100"), null, null);
        assertEquals(ResultFlag.HIGH, numeric("160", ldl));
        assertEquals(ResultFlag.NORMAL, numeric("60", ldl));
    }

    @Test
    void noRangeMeansNoInterpretation() {
        assertEquals(ResultFlag.NONE, numeric("5", null));
        assertEquals(ResultFlag.NONE, numeric("5", new ResolvedRange(null, null, null, null)));
    }

    @Test
    void missingValueHasNoFlag() {
        assertNull(ResultFlagEvaluator.evaluate(ValueType.NUMERIC, null, null, GLUCOSE, List.of()));
        assertNull(ResultFlagEvaluator.evaluate(ValueType.TEXT, null, "  ", null, List.of("Nil")));
    }

    @Test
    void textResultsAreAbnormalUnlessInTheNormalList() {
        List<String> normal = List.of("Nil", "Negative");
        assertEquals(ResultFlag.NORMAL, ResultFlagEvaluator.evaluate(ValueType.TEXT, null, "nil", null, normal));
        assertEquals(ResultFlag.NORMAL, ResultFlagEvaluator.evaluate(ValueType.TEXT, null, " Negative ", null, normal));
        assertEquals(ResultFlag.ABNORMAL, ResultFlagEvaluator.evaluate(ValueType.TEXT, null, "2+", null, normal));
        assertEquals(ResultFlag.NONE, ResultFlagEvaluator.evaluate(ValueType.TEXT, null, "Yellow", null, List.of()));
    }

    @Test
    void narrativeIsNeverFlagged() {
        assertEquals(ResultFlag.NONE, ResultFlagEvaluator.evaluate(ValueType.NARRATIVE, null, "Clear lung fields", null, List.of()));
    }

    private static ResultFlag numeric(String value, ResolvedRange range) {
        return ResultFlagEvaluator.evaluate(ValueType.NUMERIC, bd(value), null, range, List.of());
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
