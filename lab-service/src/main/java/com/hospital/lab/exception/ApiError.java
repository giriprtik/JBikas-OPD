package com.hospital.lab.exception;

import java.time.Instant;
import java.util.Map;

/** Uniform error body. The frontend shows {@code message} directly, so keep it human-readable. */
public record ApiError(int status, String error, String message, Map<String, String> fieldErrors, Instant timestamp) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Map.of(), Instant.now());
    }
}
