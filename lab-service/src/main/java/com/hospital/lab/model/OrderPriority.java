package com.hospital.lab.model;

/** STAT = immediately (life-threatening), URGENT = within the hour, ROUTINE = normal turnaround. */
public enum OrderPriority {
    STAT(0), URGENT(1), ROUTINE(2);

    private final int rank;

    OrderPriority(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
