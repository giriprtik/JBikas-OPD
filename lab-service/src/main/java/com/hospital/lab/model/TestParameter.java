package com.hospital.lab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** One measured value within a test, e.g. Haemoglobin within CBC. */
@Entity
@Table(name = "test_parameters")
public class TestParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private TestDefinition test;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ValueType valueType;

    @Column(nullable = false)
    private int decimals;

    /** Comma-separated normal values for TEXT parameters, e.g. "Nil,Negative". */
    @Column(length = 255)
    private String normalValues;

    @Column(nullable = false)
    private int sortOrder;

    @OneToMany(mappedBy = "parameter", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<ReferenceRange> ranges = new ArrayList<>();

    public Long getId() { return id; }
    public TestDefinition getTest() { return test; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public ValueType getValueType() { return valueType; }
    public int getDecimals() { return decimals; }
    public String getNormalValues() { return normalValues; }
    public int getSortOrder() { return sortOrder; }
    public List<ReferenceRange> getRanges() { return ranges; }

    public List<String> normalValueList() {
        return parseNormalValues(normalValues);
    }

    public static List<String> parseNormalValues(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
