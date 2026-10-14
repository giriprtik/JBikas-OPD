package com.hospital.lab.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_orders")
public class LabOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId; // References the patient ID maintained inside opd-service

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String testType; // e.g., "CBC", "Thyroid Profile", "Urine RE"

    private String testResult;
    private String status; // PENDING, COMPLETED
    private LocalDateTime orderTime;

    public LabOrder() {}
    public LabOrder(Long patientId, String patientName, String testType) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.testType = testType;
        this.status = "PENDING";
        this.orderTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    public String getTestResult() { return testResult; }
    public void setTestResult(String testResult) { this.testResult = testResult; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getOrderTime() { return orderTime; }
}
