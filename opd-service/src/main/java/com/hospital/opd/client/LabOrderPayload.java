package com.hospital.opd.client;

import java.util.List;

/** Body of POST /api/lab/orders. Patient demographics travel with the order so lab can pick reference ranges. */
public record LabOrderPayload(
        Long visitId,
        Long patientId,
        String mrn,
        String patientName,
        String patientGender,
        int patientAgeYears,
        String orderingDoctorName,
        List<String> testCodes,
        String priority,
        String clinicalNotes) {
}
