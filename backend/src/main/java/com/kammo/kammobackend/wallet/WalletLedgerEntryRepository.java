package com.kammo.kammobackend.wallet;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletLedgerEntryRepository extends JpaRepository<WalletLedgerEntry, Long> {

    List<WalletLedgerEntry> findByWalletAccountIdOrderByCreatedAtDesc(Long walletAccountId);

    List<WalletLedgerEntry> findByDealIdOrderByCreatedAtDesc(Long dealId);

    boolean existsByPaymentRecordId(Long paymentRecordId);
}
