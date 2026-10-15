package com.hospital.lab.dto;

import com.hospital.lab.model.ValueType;

import java.math.BigDecimal;
import java.util.List;

public record CatalogParameterResponse(String code, String name, String unit, ValueType valueType, int decimals,
                                       List<String> normalValues, List<RangeResponse> ranges) {

    public record RangeResponse(String gender, int ageMinYears, Integer ageMaxYears, BigDecimal low, BigDecimal high,
                                BigDecimal criticalLow, BigDecimal criticalHigh) {
    }
}
