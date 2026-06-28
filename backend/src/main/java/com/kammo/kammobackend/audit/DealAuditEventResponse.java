package com.kammo.kammobackend.audit;

import java.time.Instant;

public record DealAuditEventResponse(
    Long id,
    Long dealId,
    DealAuditEventType eventType,
    Long actorUserId,
    String description,
    String metadata,
    Instant createdAt
) {

    public static DealAuditEventResponse from(DealAuditEvent event) {
        return new DealAuditEventResponse(
            event.getId(),
            event.getDealId(),
            event.getEventType(),
            event.getActorUserId(),
            event.getDescription(),
            event.getMetadata(),
            event.getCreatedAt()
        );
    }
}
