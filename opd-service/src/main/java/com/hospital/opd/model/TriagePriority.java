package com.hospital.opd.model;

/** Triage category set by the nurse. Lower rank is seen first. */
public enum TriagePriority {
    EMERGENCY(0), URGENT(1), NORMAL(2);

    private final int rank;

    TriagePriority(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
