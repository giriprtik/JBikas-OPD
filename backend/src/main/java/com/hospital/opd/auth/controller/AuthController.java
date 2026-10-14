package com.hospital.opd.auth.controller;

import com.hospital.opd.auth.jwt.JwtUtils;
import com.hospital.opd.auth.model.Role;
import com.hospital.opd.auth.model.User;
import com.hospital.opd.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registerRequest) {
        String username = (String) registerRequest.get("username");
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is already taken!"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode((String) registerRequest.get("password")));
        user.setFullName((String) registerRequest.get("fullName"));

        String roleStr = (String) registerRequest.get("role"); // "DOCTOR" or "NURSE"
        Role role = "DOCTOR".equalsIgnoreCase(roleStr) ? Role.ROLE_DOCTOR : Role.ROLE_NURSE;
        user.setRoles(Set.of(role));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }
}