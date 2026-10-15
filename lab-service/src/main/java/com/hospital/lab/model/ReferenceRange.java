package com.hospital.lab.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Normal and critical limits for a parameter, by sex and age band.
 * {@code gender} is ANY, MALE or FEMALE. Age band is [ageMinYears, ageMaxYears); ageMaxYears null = no upper limit.
 * Any limit may be null (e.g. HDL has only a lower limit, LDL only an upper one).
 */
@Entity
@Table(name = "reference_ranges")
public class ReferenceRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parameter_id", nullable = false)
    private TestParameter parameter;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false)
    private int ageMinYears;

    private Integer ageMaxYears;

    @Column(precision = 14, scale = 4)
    private BigDecimal low;
    @Column(precision = 14, scale = 4)
    private BigDecimal high;
    @Column(precision = 14, scale = 4)
    private BigDecimal criticalLow;
    @Column(precision = 14, scale = 4)
    private BigDecimal criticalHigh;

    public ReferenceRange() {
    }

    /** For tests and in-memory use. */
    public ReferenceRange(String gender, int ageMinYears, Integer ageMaxYears, BigDecimal low, BigDecimal high,
                          BigDecimal criticalLow, BigDecimal criticalHigh) {
        this.gender = gender;
        this.ageMinYears = ageMinYears;
        this.ageMaxYears = ageMaxYears;
        this.low = low;
        this.high = high;
        this.criticalLow = criticalLow;
        this.criticalHigh = criticalHigh;
    }

    public Long getId() { return id; }
    public TestParameter getParameter() { return parameter; }
    public String getGender() { return gender; }
    public int getAgeMinYears() { return ageMinYears; }
    public Integer getAgeMaxYears() { return ageMaxYears; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getCriticalLow() { return criticalLow; }
    public BigDecimal getCriticalHigh() { return criticalHigh; }
}
