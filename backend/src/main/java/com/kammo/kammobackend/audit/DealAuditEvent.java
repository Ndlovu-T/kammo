package com.kammo.kammobackend.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Append-only audit record for a deal. No setters are exposed and no service
 * in the codebase should ever update or delete a row in this table.
 */
@Entity
@Table(name = "deal_audit_events")
public class DealAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DealAuditEventType eventType;

    private Long actorUserId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 4000)
    private String metadata;

    @Column(nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    protected DealAuditEvent() {
    }

    public DealAuditEvent(
        Long dealId,
        DealAuditEventType eventType,
        Long actorUserId,
        String description,
        String metadata
    ) {
        this.dealId = dealId;
        this.eventType = eventType;
        this.actorUserId = actorUserId;
        this.description = description;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public Long getDealId() {
        return dealId;
    }

    public DealAuditEventType getEventType() {
        return eventType;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getDescription() {
        return description;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
