package com.hospital.lab.dto;

import java.util.List;

public record CatalogTestResponse(String code, String name, String category, String specimen, int turnaroundHours,
                                  List<CatalogParameterResponse> parameters) {
}
