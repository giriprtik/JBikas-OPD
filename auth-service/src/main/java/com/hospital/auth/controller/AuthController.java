package com.hospital.auth.controller;

import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.LoginResponse;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.dto.UserResponse;
import com.hospital.auth.exception.BusinessRuleException;
import com.hospital.auth.jwt.JwtUtils;
import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;
import com.hospital.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    /** Public. Bad credentials surface as 401 through GlobalExceptionHandler. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String fullName = userRepository.findByUsername(principal.getUsername())
                .map(User::getFullName)
                .orElse(principal.getUsername());
        List<String> roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList();
        return new LoginResponse(jwtUtils.generateJwtToken(principal, fullName), principal.getUsername(), fullName, roles);
    }

    /** Staff accounts are created by an administrator only; there is no self sign-up in a hospital system. */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessRuleException("Username '" + request.username() + "' is already taken.");
        }
        Role role;
        try {
            role = Role.fromName(request.role());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role '" + request.role() + "'. Use DOCTOR, NURSE, LAB_TECH or ADMIN.");
        }
        User user = new User(request.username(), encoder.encode(request.password()), request.fullName().trim(), new HashSet<>(Set.of(role)));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userRepository.save(user)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }
}
