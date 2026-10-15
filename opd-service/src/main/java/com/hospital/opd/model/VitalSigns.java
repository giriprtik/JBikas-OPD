package com.hospital.opd.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** One set of vitals. A visit may have several (re-triage); the latest is shown by default. */
@Entity
@Table(name = "vital_signs")
public class VitalSigns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false, updatable = false)
    private Visit visit;

    @Column(name = "temperature_c", precision = 4, scale = 1)
    private BigDecimal temperatureC;
    private Integer pulseBpm;
    private Integer respiratoryRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer spo2;
    @Column(precision = 5, scale = 1)
    private BigDecimal weightKg;
    @Column(precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(nullable = false, updatable = false, length = 50)
    private String recordedBy;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    public Long getId() { return id; }
    public Visit getVisit() { return visit; }
    public void setVisit(Visit visit) { this.visit = visit; }
    public BigDecimal getTemperatureC() { return temperatureC; }
    public void setTemperatureC(BigDecimal temperatureC) { this.temperatureC = temperatureC; }
    public Integer getPulseBpm() { return pulseBpm; }
    public void setPulseBpm(Integer pulseBpm) { this.pulseBpm = pulseBpm; }
    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }
    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }
    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
