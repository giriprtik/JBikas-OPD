package com.hospital.opd.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitStatusTest {

    @Test
    void normalPathIsAllowed() {
        assertTrue(VisitStatus.REGISTERED.canMoveTo(VisitStatus.TRIAGED));
        assertTrue(VisitStatus.TRIAGED.canMoveTo(VisitStatus.IN_CONSULTATION));
        assertTrue(VisitStatus.IN_CONSULTATION.canMoveTo(VisitStatus.AWAITING_LAB));
        assertTrue(VisitStatus.AWAITING_LAB.canMoveTo(VisitStatus.LAB_RESULTS_READY));
        assertTrue(VisitStatus.LAB_RESULTS_READY.canMoveTo(VisitStatus.IN_CONSULTATION));
        assertTrue(VisitStatus.IN_CONSULTATION.canMoveTo(VisitStatus.COMPLETED));
    }

    @Test
    void closedVisitsCannotChange() {
        for (VisitStatus target : VisitStatus.values()) {
            assertFalse(VisitStatus.COMPLETED.canMoveTo(target));
            assertFalse(VisitStatus.CANCELLED.canMoveTo(target));
        }
    }

    @Test
    void cannotSkipTheDoctorOrCancelOnceSeen() {
        assertFalse(VisitStatus.TRIAGED.canMoveTo(VisitStatus.COMPLETED));
        assertFalse(VisitStatus.IN_CONSULTATION.canMoveTo(VisitStatus.CANCELLED));
        assertFalse(VisitStatus.AWAITING_LAB.canMoveTo(VisitStatus.COMPLETED));
    }
}
