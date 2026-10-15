package com.hospital.lab.exception;

/** Another microservice failed or was unreachable (HTTP 502). */
public class DownstreamServiceException extends RuntimeException {
    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
