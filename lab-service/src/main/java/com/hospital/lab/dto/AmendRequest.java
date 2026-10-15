package com.hospital.lab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AmendRequest(@Size(max = 4000) String value, @NotBlank @Size(max = 500) String reason) {
}
