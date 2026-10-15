package com.hospital.lab.model;

/**
 * Interpretation of a result. CRITICAL_* are "panic values": the lab must phone the clinician
 * before releasing, and the release records who was informed.
 */
public enum ResultFlag {
    NORMAL, LOW, HIGH, CRITICAL_LOW, CRITICAL_HIGH, ABNORMAL, NONE;

    public boolean isCritical() {
        return this == CRITICAL_LOW || this == CRITICAL_HIGH;
    }

    public boolean isAbnormal() {
        return this != NORMAL && this != NONE;
    }
}
