package com.hospital.opd.dto;

import jakarta.validation.constraints.NotNull;

/** Sent by lab-service when a report is released. */
public record LabResultsReadyRequest(@NotNull Long labOrderId, String orderNumber, boolean hasCriticalResults) {
}
