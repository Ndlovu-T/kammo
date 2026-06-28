package com.kammo.kammobackend.wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletLedgerEntryResponse(
    Long id,
    WalletLedgerEntryType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    String reason,
    Long dealId,
    Instant createdAt
) {
    public static WalletLedgerEntryResponse from(WalletLedgerEntry entry) {
        return new WalletLedgerEntryResponse(
            entry.getId(),
            entry.getType(),
            entry.getAmount(),
            entry.getBalanceAfter(),
            entry.getReason(),
            entry.getDealId(),
            entry.getCreatedAt()
        );
    }
}
