package com.hospital.lab.dto;

import java.time.Instant;

public record AttachmentResponse(Long id, String originalFilename, String contentType, long sizeBytes,
                                 String description, String uploadedBy, Instant uploadedAt) {
}
