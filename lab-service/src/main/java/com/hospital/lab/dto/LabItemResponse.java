package com.hospital.lab.dto;

import java.util.List;

public record LabItemResponse(Long id, String testCode, String testName, String category, String specimen,
                              List<LabResultResponse> results) {
}
