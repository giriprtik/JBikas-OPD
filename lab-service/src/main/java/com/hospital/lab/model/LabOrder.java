package com.hospital.lab.model;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A lab request for one OPD visit. Patient demographics are copied in at order time so the lab
 * works without calling OPD, and so the report reflects the patient as they were when tested.
 */
@Entity
@Table(name = "lab_orders")
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(nullable = false)
    private Long visitId;
    @Column(nullable = false)
    private Long patientId;
    @Column(nullable = false, length = 20)
    private String mrn;
    @Column(nullable = false, length = 150)
    private String patientName;
    @Column(nullable = false, length = 10)
    private String patientGender;
    @Column(nullable = false)
    private int patientAgeYears;

    @Column(nullable = false, length = 50)
    private String orderingDoctor;
    @Column(length = 150)
    private String orderingDoctorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderPriority priority;

    private String clinicalNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LabOrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant orderedAt;
    private Instant sampleCollectedAt;
    @Column(length = 50)
    private String sampleCollectedBy;
    private Instant releasedAt;
    @Column(length = 50)
    private String releasedBy;
    @Column(length = 150)
    private String releasedByName;
    @Column(length = 150)
    private String criticalNotifiedTo;
    private String cancelReason;

    @Column(nullable = false)
    private boolean amended;

    @Version
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<LabOrderItem> items = new ArrayList<>();

    public void addItem(LabOrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    /** All results across items, in report order. */
    public List<LabResult> allResults() {
        return items.stream().flatMap(i -> i.getResults().stream()).toList();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getVisitId() { return visitId; }
    public void setVisitId(Long visitId) { this.visitId = visitId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getMrn() { return mrn; }
    public void setMrn(String mrn) { this.mrn = mrn; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    public int getPatientAgeYears() { return patientAgeYears; }
    public void setPatientAgeYears(int patientAgeYears) { this.patientAgeYears = patientAgeYears; }
    public String getOrderingDoctor() { return orderingDoctor; }
    public void setOrderingDoctor(String orderingDoctor) { this.orderingDoctor = orderingDoctor; }
    public String getOrderingDoctorName() { return orderingDoctorName; }
    public void setOrderingDoctorName(String orderingDoctorName) { this.orderingDoctorName = orderingDoctorName; }
    public OrderPriority getPriority() { return priority; }
    public void setPriority(OrderPriority priority) { this.priority = priority; }
    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }
    public LabOrderStatus getStatus() { return status; }
    public void setStatus(LabOrderStatus status) { this.status = status; }
    public Instant getOrderedAt() { return orderedAt; }
    public void setOrderedAt(Instant orderedAt) { this.orderedAt = orderedAt; }
    public Instant getSampleCollectedAt() { return sampleCollectedAt; }
    public void setSampleCollectedAt(Instant sampleCollectedAt) { this.sampleCollectedAt = sampleCollectedAt; }
    public String getSampleCollectedBy() { return sampleCollectedBy; }
    public void setSampleCollectedBy(String sampleCollectedBy) { this.sampleCollectedBy = sampleCollectedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }
    public String getReleasedByName() { return releasedByName; }
    public void setReleasedByName(String releasedByName) { this.releasedByName = releasedByName; }
    public String getCriticalNotifiedTo() { return criticalNotifiedTo; }
    public void setCriticalNotifiedTo(String criticalNotifiedTo) { this.criticalNotifiedTo = criticalNotifiedTo; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public boolean isAmended() { return amended; }
    public void setAmended(boolean amended) { this.amended = amended; }
    public long getVersion() { return version; }
    public List<LabOrderItem> getItems() { return items; }
}
