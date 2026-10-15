package com.hospital.opd.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/** One OPD encounter: registration, triage, consultation and lab referrals for a single day. */
@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, updatable = false)
    private Patient patient;

    @Column(nullable = false, updatable = false)
    private LocalDate visitDate;

    @Column(nullable = false, updatable = false)
    private int tokenNumber;

    @Column(nullable = false)
    private String chiefComplaint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TriagePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private VisitStatus status;

    @Column(length = 50)
    private String assignedDoctor;

    @Column(length = 150)
    private String assignedDoctorName;

    private String consultationNotes;
    private String diagnosis;
    private Long latestLabOrderId;
    private String cancelReason;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(nullable = false, updatable = false, length = 50)
    private String registeredBy;

    private Instant consultationStartedAt;
    private Instant completedAt;

    @Version
    private long version;

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public int getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(int tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public TriagePriority getPriority() { return priority; }
    public void setPriority(TriagePriority priority) { this.priority = priority; }
    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }
    public String getAssignedDoctor() { return assignedDoctor; }
    public void setAssignedDoctor(String assignedDoctor) { this.assignedDoctor = assignedDoctor; }
    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }
    public String getConsultationNotes() { return consultationNotes; }
    public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public Long getLatestLabOrderId() { return latestLabOrderId; }
    public void setLatestLabOrderId(Long latestLabOrderId) { this.latestLabOrderId = latestLabOrderId; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }
    public String getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = registeredBy; }
    public Instant getConsultationStartedAt() { return consultationStartedAt; }
    public void setConsultationStartedAt(Instant consultationStartedAt) { this.consultationStartedAt = consultationStartedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public long getVersion() { return version; }
}
