package com.hospital.opd.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Append-only audit record. Never updated or deleted. */
@Entity
@Table(name = "visit_events")
public class VisitEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false, updatable = false)
    private Visit visit;

    @Enumerated(EnumType.STRING)
    @Column(length = 25, updatable = false)
    private VisitStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25, updatable = false)
    private VisitStatus toStatus;

    @Column(nullable = false, length = 50, updatable = false)
    private String actor;

    @Column(updatable = false)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected VisitEvent() {
    }

    public VisitEvent(Visit visit, VisitStatus fromStatus, VisitStatus toStatus, String actor, String note, Instant occurredAt) {
        this.visit = visit;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actor = actor;
        this.note = note;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public Visit getVisit() { return visit; }
    public VisitStatus getFromStatus() { return fromStatus; }
    public VisitStatus getToStatus() { return toStatus; }
    public String getActor() { return actor; }
    public String getNote() { return note; }
    public Instant getOccurredAt() { return occurredAt; }
}
