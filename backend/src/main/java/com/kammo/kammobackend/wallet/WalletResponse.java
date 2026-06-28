package com.kammo.kammobackend.wallet;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@code balance} is an internal credit figure tracked by Kammo, not money Kammo holds in its
 * own bank account — it always references funds that are either still with Paystack/Ozow or
 * already paid out to the seller's bank account.
 */
public record WalletResponse(
    BigDecimal balance,
    List<WalletLedgerEntryResponse> recentEntries
) {
    public static WalletResponse from(WalletAccount wallet, List<WalletLedgerEntry> entries) {
        return new WalletResponse(
            wallet.getBalance(),
            entries.stream().map(WalletLedgerEntryResponse::from).toList()
        );
    }
}
