package com.hospital.lab.dto;

import jakarta.validation.constraints.Size;

/** {@code criticalNotifiedTo} is required when any result is critical: who was phoned, e.g. "Dr. Shrestha, 14:20". */
public record ReleaseRequest(@Size(max = 150) String criticalNotifiedTo) {
}
