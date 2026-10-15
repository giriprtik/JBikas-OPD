package com.hospital.lab.service;

import java.math.BigDecimal;

/** The limits that apply to one patient for one parameter. Any limit may be null. */
public record ResolvedRange(BigDecimal low, BigDecimal high, BigDecimal criticalLow, BigDecimal criticalHigh) {
}
