package com.hospital.opd.security;

import java.security.Principal;

/** The signed-in staff member, built from the JWT issued by auth-service. */
public record StaffPrincipal(String username, String fullName) implements Principal {

    @Override
    public String getName() {
        return username;
    }
}
