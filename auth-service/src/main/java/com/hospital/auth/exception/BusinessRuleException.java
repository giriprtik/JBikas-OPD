package com.hospital.auth.exception;

/** A request that is well-formed but conflicts with current state (maps to 409). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
