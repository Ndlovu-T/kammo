package com.kammo.kammobackend.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_records")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentRecordType type;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(length = 100)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentRecord() {
    }

    public PaymentRecord(
        Long dealId,
        PaymentRecordType type,
        String provider,
        String providerReference,
        PaymentStatus status,
        BigDecimal amount
    ) {
        this.dealId = dealId;
        this.type = type;
        this.provider = provider;
        this.providerReference = providerReference;
        this.status = status;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Long getDealId() {
        return dealId;
    }

    public PaymentRecordType getType() {
        return type;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
