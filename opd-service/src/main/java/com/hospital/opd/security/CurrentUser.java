package com.hospital.opd.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Access to the signed-in staff member inside services. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static StaffPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof StaffPrincipal principal) {
            return principal;
        }
        throw new IllegalStateException("No authenticated staff member in the security context");
    }

    public static String username() {
        return get().username();
    }
}
