package com.hospital.auth.config;

import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;
import com.hospital.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default Doctor account if it does not exist
        if (!userRepository.existsByUsername("doctor1")) {
            User doctor = new User(
                "doctor1",
                passwordEncoder.encode("doctor123"),
                "Dr. Shrestha",
                new HashSet<>(Collections.singletonList(Role.ROLE_DOCTOR))
            );
            userRepository.save(doctor);
        }

        // Seed default Nurse account if it does not exist
        if (!userRepository.existsByUsername("nurse1")) {
            User nurse = new User(
                "nurse1",
                passwordEncoder.encode("nurse123"),
                "Nurse Basnet",
                new HashSet<>(Collections.singletonList(Role.ROLE_NURSE))
            );
            userRepository.save(nurse);
        }
    }
}
