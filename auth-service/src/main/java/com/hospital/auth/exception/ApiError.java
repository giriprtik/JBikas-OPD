package com.hospital.auth.exception;

import java.time.Instant;
import java.util.Map;

/** Uniform error body returned by every endpoint. */
public record ApiError(int status, String error, String message, Map<String, String> fieldErrors, Instant timestamp) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Map.of(), Instant.now());
    }
}
