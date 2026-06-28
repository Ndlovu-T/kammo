package com.kammo.kammobackend.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DealAuditServiceTest {

    @Mock
    private DealAuditEventRepository repository;

    @Captor
    private ArgumentCaptor<DealAuditEvent> eventCaptor;

    private DealAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new DealAuditService(repository, new ObjectMapper());
    }

    @Test
    void record_serializesMetadataAsJson() {
        auditService.record(
            10L, DealAuditEventType.STATUS_CHANGED, 1L, "Status changed",
            Map.of("oldStatus", "CREATED", "newStatus", "SELLER_ACCEPTED")
        );

        verify(repository).save(eventCaptor.capture());
        DealAuditEvent saved = eventCaptor.getValue();
        assertThat(saved.getDealId()).isEqualTo(10L);
        assertThat(saved.getEventType()).isEqualTo(DealAuditEventType.STATUS_CHANGED);
        assertThat(saved.getActorUserId()).isEqualTo(1L);
        assertThat(saved.getMetadata()).contains("\"oldStatus\":\"CREATED\"");
    }

    @Test
    void record_allowsNullActorForSystemGeneratedEvents() {
        auditService.record(10L, DealAuditEventType.PAYMENT_ACTION, null, "Payout succeeded", Map.of());

        verify(repository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getActorUserId()).isNull();
        assertThat(eventCaptor.getValue().getMetadata()).isNull();
    }

    @Test
    void getAuditTrail_mapsRepositoryEventsInOrder() {
        DealAuditEvent first = new DealAuditEvent(10L, DealAuditEventType.STATUS_CHANGED, 1L, "Created", null);
        DealAuditEvent second = new DealAuditEvent(10L, DealAuditEventType.MESSAGE_SENT, 2L, "Message sent", null);
        when(repository.findByDealIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(first, second));

        List<DealAuditEventResponse> trail = auditService.getAuditTrail(10L);

        assertThat(trail).hasSize(2);
        assertThat(trail.get(0).description()).isEqualTo("Created");
        assertThat(trail.get(1).eventType()).isEqualTo(DealAuditEventType.MESSAGE_SENT);
    }
}
