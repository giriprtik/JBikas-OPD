package com.hospital.opd.common.config;

import com.hospital.opd.auth.model.Role;
import com.hospital.opd.auth.model.User;
import com.hospital.opd.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

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
        if (!userRepository.existsByUsername("doctor1")) {
            User doctor = new User();
            doctor.setUsername("doctor1");
            doctor.setPassword(passwordEncoder.encode("doctor123"));
            doctor.setFullName("Dr. Shrestha");
            doctor.setRoles(Set.of(Role.ROLE_DOCTOR));
            userRepository.save(doctor);
        }

        if (!userRepository.existsByUsername("nurse1")) {
            User nurse = new User();
            nurse.setUsername("nurse1");
            nurse.setPassword(passwordEncoder.encode("nurse123"));
            nurse.setFullName("Nurse Basnet");
            nurse.setRoles(Set.of(Role.ROLE_NURSE));
            userRepository.save(nurse);
        }
    }
}