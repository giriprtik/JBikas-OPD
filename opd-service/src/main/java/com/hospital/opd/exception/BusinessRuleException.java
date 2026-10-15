package com.hospital.opd.exception;

/** The request is valid but not allowed in the current state of the record (HTTP 409). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
