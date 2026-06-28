package com.kammo.kammobackend.audit;

import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DealStatusChangedEvent;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DealStatusAuditListener {

    private final DealAuditService auditService;

    public DealStatusAuditListener(DealAuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onStatusChanged(DealStatusChangedEvent event) {
        DealAuditEventType eventType = resolveEventType(event.fromStatus(), event.toStatus());
        auditService.record(
            event.dealId(),
            eventType,
            null,
            "Deal status changed from " + event.fromStatus() + " to " + event.toStatus(),
            Map.of("oldStatus", event.fromStatus().name(), "newStatus", event.toStatus().name())
        );
    }

    private DealAuditEventType resolveEventType(DealStatus fromStatus, DealStatus toStatus) {
        if (toStatus == DealStatus.DISPUTED) {
            return DealAuditEventType.DISPUTE_OPENED;
        }
        if (fromStatus == DealStatus.DISPUTED) {
            return DealAuditEventType.DISPUTE_RESOLVED;
        }
        return DealAuditEventType.STATUS_CHANGED;
    }
}
