package com.hospital.lab.service;

import com.hospital.lab.dto.CatalogParameterResponse;
import com.hospital.lab.dto.CatalogTestResponse;
import com.hospital.lab.repository.TestDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final TestDefinitionRepository tests;

    public CatalogService(TestDefinitionRepository tests) {
        this.tests = tests;
    }

    @Transactional(readOnly = true)
    public List<CatalogTestResponse> activeTests() {
        return tests.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(t -> new CatalogTestResponse(t.getCode(), t.getName(), t.getCategory(), t.getSpecimen(),
                        t.getTurnaroundHours(),
                        t.getParameters().stream()
                                .map(p -> new CatalogParameterResponse(p.getCode(), p.getName(), p.getUnit(),
                                        p.getValueType(), p.getDecimals(), p.normalValueList(),
                                        p.getRanges().stream()
                                                .map(r -> new CatalogParameterResponse.RangeResponse(r.getGender(),
                                                        r.getAgeMinYears(), r.getAgeMaxYears(), r.getLow(), r.getHigh(),
                                                        r.getCriticalLow(), r.getCriticalHigh()))
                                                .toList()))
                                .toList()))
                .toList();
    }
}
