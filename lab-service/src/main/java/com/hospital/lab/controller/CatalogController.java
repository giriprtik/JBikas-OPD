package com.hospital.lab.controller;

import com.hospital.lab.dto.CatalogTestResponse;
import com.hospital.lab.service.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /** Orderable tests with their parameters and reference ranges. */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE', 'LAB_TECH')")
    public List<CatalogTestResponse> list() {
        return catalogService.activeTests();
    }
}
