package com.kammo.kammobackend.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DealAuditService {

    private final DealAuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public DealAuditService(DealAuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void record(
        Long dealId,
        DealAuditEventType eventType,
        Long actorUserId,
        String description,
        Map<String, Object> metadata
    ) {
        repository.save(new DealAuditEvent(dealId, eventType, actorUserId, description, toJson(metadata)));
    }

    public List<DealAuditEventResponse> getAuditTrail(Long dealId) {
        return repository.findByDealIdOrderByCreatedAtAsc(dealId).stream()
            .map(DealAuditEventResponse::from)
            .toList();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit event metadata", e);
        }
    }
}
