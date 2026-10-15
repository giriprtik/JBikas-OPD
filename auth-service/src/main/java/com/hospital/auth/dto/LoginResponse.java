package com.hospital.auth.dto;

import java.util.List;

public record LoginResponse(String token, String username, String fullName, List<String> roles) {
}
