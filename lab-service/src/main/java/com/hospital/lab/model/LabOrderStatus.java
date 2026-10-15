package com.hospital.lab.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * ORDERED → SAMPLE_COLLECTED → IN_PROGRESS → RELEASED. Any status before RELEASED may go to CANCELLED.
 * Released results can still be amended (with a reason) but the order stays RELEASED.
 */
public enum LabOrderStatus {
    ORDERED, SAMPLE_COLLECTED, IN_PROGRESS, RELEASED, CANCELLED;

    private static final Map<LabOrderStatus, Set<LabOrderStatus>> TRANSITIONS = Map.of(
            ORDERED, EnumSet.of(SAMPLE_COLLECTED, CANCELLED),
            SAMPLE_COLLECTED, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, EnumSet.of(RELEASED, CANCELLED),
            RELEASED, EnumSet.noneOf(LabOrderStatus.class),
            CANCELLED, EnumSet.noneOf(LabOrderStatus.class)
    );

    public boolean canMoveTo(LabOrderStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }
}
