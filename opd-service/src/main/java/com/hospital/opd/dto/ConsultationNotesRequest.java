package com.hospital.opd.dto;

import jakarta.validation.constraints.Size;

public record ConsultationNotesRequest(
        @Size(max = 10000) String consultationNotes,
        @Size(max = 2000) String diagnosis) {
}
