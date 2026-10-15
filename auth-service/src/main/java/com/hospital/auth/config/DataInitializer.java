package com.hospital.auth.config;

import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;
import com.hospital.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Seeds one account per role for development. Disable in production with APP_SEED_DEFAULT_USERS=false
 * and create real accounts through POST /api/auth/register as admin.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.default-users:true}")
    private boolean seedDefaultUsers;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedDefaultUsers) {
            return;
        }
        seed("admin1", "admin123", "System Administrator", Role.ROLE_ADMIN);
        seed("doctor1", "doctor123", "Dr. Shrestha", Role.ROLE_DOCTOR);
        seed("nurse1", "nurse123", "Nurse Basnet", Role.ROLE_NURSE);
        seed("labtech1", "lab123", "Lab Tech Gurung", Role.ROLE_LAB_TECH);
    }

    private void seed(String username, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(new User(username, passwordEncoder.encode(rawPassword), fullName, new HashSet<>(Set.of(role))));
        log.warn("Seeded default account '{}' ({}). Change its password before going live.", username, role);
    }
}
