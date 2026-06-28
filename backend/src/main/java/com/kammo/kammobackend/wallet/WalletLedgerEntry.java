package com.kammo.kammobackend.wallet;

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

/**
 * Append-only audit trail of every wallet balance change. Every entry carries a signed
 * {@code amount} (positive for credits such as HOLD/RELEASE, negative for debits such as
 * PAYOUT/REFUND/REVERSAL) plus the resulting {@code balanceAfter}, so the full history of a
 * wallet's balance can always be reconstructed and reconciled against {@code PaymentRecord}.
 * Rows are never updated or deleted.
 */
@Entity
@Table(name = "wallet_ledger_entries")
public class WalletLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long walletAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletLedgerEntryType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 255)
    private String reason;

    private Long dealId;

    private Long paymentRecordId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected WalletLedgerEntry() {
    }

    public WalletLedgerEntry(
        Long walletAccountId,
        WalletLedgerEntryType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String reason,
        Long dealId,
        Long paymentRecordId
    ) {
        this.walletAccountId = walletAccountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reason = reason;
        this.dealId = dealId;
        this.paymentRecordId = paymentRecordId;
    }

    public Long getId() {
        return id;
    }

    public Long getWalletAccountId() {
        return walletAccountId;
    }

    public WalletLedgerEntryType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReason() {
        return reason;
    }

    public Long getDealId() {
        return dealId;
    }

    public Long getPaymentRecordId() {
        return paymentRecordId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
