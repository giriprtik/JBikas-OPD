package com.hospital.opd.dto;

import com.hospital.opd.model.VisitStatus;

import java.time.Instant;

public record VisitEventResponse(VisitStatus fromStatus, VisitStatus toStatus, String actor, String note, Instant occurredAt) {
}
