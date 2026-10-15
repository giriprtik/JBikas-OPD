package com.hospital.auth.dto;

import com.hospital.auth.model.Role;
import com.hospital.auth.model.User;

import java.util.List;

public record UserResponse(Long id, String username, String fullName, List<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getRoles().stream().map(Role::name).sorted().toList());
    }
}
