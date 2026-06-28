package com.kammo.kammobackend.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DealStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DealStatusAuditListenerTest {

    @Mock
    private DealAuditService auditService;

    private DealStatusAuditListener listener;

    @BeforeEach
    void setUp() {
        listener = new DealStatusAuditListener(auditService);
    }

    private DealStatusChangedEvent eventFor(DealStatus from, DealStatus to) {
        return new DealStatusChangedEvent(10L, "DEAL0001", "Widget", 1L, DealRole.BUYER, "0710000002", from, to);
    }

    @Test
    void onStatusChanged_recordsDisputeOpenedWhenTransitioningIntoDisputed() {
        listener.onStatusChanged(eventFor(DealStatus.IN_TRANSIT, DealStatus.DISPUTED));

        verify(auditService).record(eq(10L), eq(DealAuditEventType.DISPUTE_OPENED), any(), any(), any());
    }

    @Test
    void onStatusChanged_recordsDisputeResolvedWhenLeavingDisputed() {
        listener.onStatusChanged(eventFor(DealStatus.DISPUTED, DealStatus.REFUNDED));

        verify(auditService).record(eq(10L), eq(DealAuditEventType.DISPUTE_RESOLVED), any(), any(), any());
    }

    @Test
    void onStatusChanged_recordsGenericStatusChangedOtherwise() {
        listener.onStatusChanged(eventFor(DealStatus.AWAITING_COLLECTION, DealStatus.IN_TRANSIT));

        verify(auditService).record(eq(10L), eq(DealAuditEventType.STATUS_CHANGED), any(), any(), any());
    }
}
