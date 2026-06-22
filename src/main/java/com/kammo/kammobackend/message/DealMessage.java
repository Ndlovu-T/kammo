package com.kammo.kammobackend.message;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "deal_messages")
public class DealMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealId;

    @Column(nullable = false)
    private Long senderUserId;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DealMessage() {
    }

    public DealMessage(Long dealId, Long senderUserId, String body) {
        this.dealId = dealId;
        this.senderUserId = senderUserId;
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public Long getDealId() {
        return dealId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
