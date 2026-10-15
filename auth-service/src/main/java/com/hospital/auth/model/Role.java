package com.hospital.auth.model;

/**
 * Staff roles. The names are written into the JWT "roles" claim and checked by
 * every service with hasRole('DOCTOR'), hasRole('NURSE'), hasRole('LAB_TECH'), hasRole('ADMIN').
 */
public enum Role {
    ROLE_DOCTOR,
    ROLE_NURSE,
    ROLE_LAB_TECH,
    ROLE_ADMIN;

    /** Accepts "doctor", "LAB_TECH" or "ROLE_NURSE" and returns the matching role. */
    public static Role fromName(String name) {
        String normalised = name.trim().toUpperCase();
        return Role.valueOf(normalised.startsWith("ROLE_") ? normalised : "ROLE_" + normalised);
    }
}
