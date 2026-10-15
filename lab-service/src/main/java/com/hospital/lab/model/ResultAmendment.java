package com.hospital.lab.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Audit record for a change to a released result. Append-only. */
@Entity
@Table(name = "result_amendments")
public class ResultAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false, updatable = false)
    private LabResult result;

    @Column(updatable = false)
    private String previousValue;
    @Column(updatable = false)
    private String newValue;
    @Enumerated(EnumType.STRING)
    @Column(length = 15, updatable = false)
    private ResultFlag previousFlag;
    @Enumerated(EnumType.STRING)
    @Column(length = 15, updatable = false)
    private ResultFlag newFlag;
    @Column(nullable = false, updatable = false)
    private String reason;
    @Column(nullable = false, length = 50, updatable = false)
    private String amendedBy;
    @Column(nullable = false, updatable = false)
    private Instant amendedAt;

    protected ResultAmendment() {
    }

    public ResultAmendment(LabResult result, String previousValue, String newValue, ResultFlag previousFlag,
                           ResultFlag newFlag, String reason, String amendedBy, Instant amendedAt) {
        this.result = result;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.previousFlag = previousFlag;
        this.newFlag = newFlag;
        this.reason = reason;
        this.amendedBy = amendedBy;
        this.amendedAt = amendedAt;
    }

    public Long getId() { return id; }
    public LabResult getResult() { return result; }
    public String getPreviousValue() { return previousValue; }
    public String getNewValue() { return newValue; }
    public ResultFlag getPreviousFlag() { return previousFlag; }
    public ResultFlag getNewFlag() { return newFlag; }
    public String getReason() { return reason; }
    public String getAmendedBy() { return amendedBy; }
    public Instant getAmendedAt() { return amendedAt; }
}
