package com.hospital.opd.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * OPD visit lifecycle. Every status change goes through {@link #canMoveTo(VisitStatus)}
 * so the workflow rules live in one place.
 *
 * <pre>
 * REGISTERED ──vitals──▶ TRIAGED ──doctor──▶ IN_CONSULTATION ──complete──▶ COMPLETED
 *     │  └──(EMERGENCY only)──────────────────▲      │   ▲
 *     │                                             lab  │ resume
 *     ▼                                              ▼   │
 *  CANCELLED ◀── (REGISTERED / TRIAGED)        AWAITING_LAB ──results──▶ LAB_RESULTS_READY
 * </pre>
 */
public enum VisitStatus {
    REGISTERED,
    TRIAGED,
    IN_CONSULTATION,
    AWAITING_LAB,
    LAB_RESULTS_READY,
    COMPLETED,
    CANCELLED;

    private static final Map<VisitStatus, Set<VisitStatus>> TRANSITIONS = Map.of(
            REGISTERED, EnumSet.of(TRIAGED, IN_CONSULTATION, CANCELLED),
            TRIAGED, EnumSet.of(IN_CONSULTATION, CANCELLED),
            IN_CONSULTATION, EnumSet.of(AWAITING_LAB, COMPLETED),
            AWAITING_LAB, EnumSet.of(LAB_RESULTS_READY, IN_CONSULTATION),
            LAB_RESULTS_READY, EnumSet.of(IN_CONSULTATION),
            COMPLETED, EnumSet.noneOf(VisitStatus.class),
            CANCELLED, EnumSet.noneOf(VisitStatus.class)
    );

    /** Statuses that still occupy the patient's single open-visit slot. */
    public static final Set<VisitStatus> ACTIVE = EnumSet.of(REGISTERED, TRIAGED, IN_CONSULTATION, AWAITING_LAB, LAB_RESULTS_READY);

    public boolean canMoveTo(VisitStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
