package com.hospital.lab.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Metadata of an uploaded file. The bytes are stored on disk under {@code storageKey} (a UUID). */
@Entity
@Table(name = "lab_attachments")
public class LabAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private LabOrder order;

    @Column(nullable = false)
    private String originalFilename;
    @Column(nullable = false, length = 100)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    @Column(nullable = false, unique = true, length = 64)
    private String storageKey;
    private String description;
    @Column(nullable = false, length = 50)
    private String uploadedBy;
    @Column(nullable = false)
    private Instant uploadedAt;

    public Long getId() { return id; }
    public LabOrder getOrder() { return order; }
    public void setOrder(LabOrder order) { this.order = order; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
