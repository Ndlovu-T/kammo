package com.kammo.kammobackend.wallet;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.payment.PaymentRecord;
import com.kammo.kammobackend.payment.PaymentRecordType;
import com.kammo.kammobackend.payment.PaymentResult;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.payment.PaymentStatus;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the Kamo Wallet — an internal credit ledger that tracks funds Paystack/Ozow is holding on
 * Kammo's behalf, broken down per seller. This entity never represents money Kammo itself
 * custodies; it is purely an accounting layer over the processor's merchant balance, so the
 * actual transfer to a seller's bank account always still goes through
 * {@code PaymentService}/{@code PaymentProvider}.
 *
 * <p>Lifecycle per deal:
 * <ul>
 *   <li>{@link #hold} — buyer's payment is verified (Deal -&gt; PAYMENT_SECURED). Credits the
 *       seller's wallet to represent funds now secured with the processor, pending delivery.</li>
 *   <li>{@link #release} — buyer confirms receipt (Deal -&gt; COMPLETED). Marks the held credit as
 *       payable; does not change the balance by itself since the credit was already on the
 *       seller's wallet — it exists purely as an audit marker preceding the payout.</li>
 *   <li>{@link #recordPayout} — called once the Paystack/Ozow transfer succeeds. Debits the
 *       wallet, zeroing out the credit that has now left the processor's balance for the
 *       seller's bank account.</li>
 *   <li>{@link #refund} / {@link #reverse} — buyer is refunded or a dispute is resolved in the
 *       buyer's favor. Debits the seller's held credit since it will never be paid out.</li>
 * </ul>
 *
 * <p>Every user has exactly one {@link WalletAccount} regardless of which side of a deal they're
 * on. Only the seller's balance actually moves (it mirrors the processor's merchant balance), but
 * the buyer's own wallet still gets a {@link #lockBuyerFunds}/{@link #unlockBuyerFunds} ledger
 * entry for the same lifecycle events so their wallet reflects money they have tied up in escrow,
 * even though it never touches their balance figure.</p>
 */
@Service
public class WalletService {

    private final WalletAccountRepository walletAccountRepository;
    private final WalletLedgerEntryRepository walletLedgerEntryRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public WalletService(
        WalletAccountRepository walletAccountRepository,
        WalletLedgerEntryRepository walletLedgerEntryRepository,
        UserRepository userRepository,
        PaymentService paymentService
    ) {
        this.walletAccountRepository = walletAccountRepository;
        this.walletLedgerEntryRepository = walletLedgerEntryRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public WalletAccount getOrCreateWallet(Long userId) {
        return walletAccountRepository.findByUserId(userId)
            .orElseGet(() -> walletAccountRepository.save(new WalletAccount(userId)));
    }

    /**
     * Buyer's payment has been verified — credit the seller's wallet to represent funds now
     * secured with Paystack/Ozow, pending delivery and buyer confirmation.
     */
    @Transactional
    public WalletLedgerEntry hold(Deal deal, BigDecimal amount, Long paymentRecordId) {
        WalletAccount wallet = getOrCreateWallet(resolveSeller(deal).getId());
        wallet.credit(amount);
        walletAccountRepository.save(wallet);
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.HOLD,
            amount,
            wallet.getBalance(),
            "Funds secured for deal " + deal.getDealCode() + ", pending buyer confirmation",
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * Buyer confirmed receipt — the previously held credit becomes payable to the seller. This is
     * an audit marker: the balance was already credited at HOLD time, so RELEASE does not move
     * money by itself. The caller is expected to follow up with the existing Paystack/Ozow payout
     * call and then {@link #recordPayout} once that transfer succeeds.
     */
    @Transactional
    public WalletLedgerEntry release(Deal deal, BigDecimal amount, Long paymentRecordId) {
        WalletAccount wallet = getOrCreateWallet(resolveSeller(deal).getId());
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.RELEASE,
            BigDecimal.ZERO,
            wallet.getBalance(),
            "Buyer confirmed receipt for deal " + deal.getDealCode() + ", credit now payable",
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * The Paystack/Ozow transfer to the seller's bank account has succeeded — debit the wallet to
     * zero out the credit that has now physically left the processor's balance.
     */
    @Transactional
    public WalletLedgerEntry recordPayout(Deal deal, BigDecimal amount, Long paymentRecordId) {
        WalletAccount wallet = getOrCreateWallet(resolveSeller(deal).getId());
        wallet.debit(amount);
        walletAccountRepository.save(wallet);
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.PAYOUT,
            amount.negate(),
            wallet.getBalance(),
            "Paystack/Ozow payout completed for deal " + deal.getDealCode(),
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * Buyer was refunded — the seller's held credit is reversed since it will never be paid out.
     */
    @Transactional
    public WalletLedgerEntry refund(Deal deal, BigDecimal amount, Long paymentRecordId) {
        return debitHeldCredit(deal, amount, paymentRecordId, WalletLedgerEntryType.REFUND,
            "Buyer refunded for deal " + deal.getDealCode() + ", held credit reversed");
    }

    /**
     * A dispute was resolved in the buyer's favor after credit was already held — reverse it.
     */
    @Transactional
    public WalletLedgerEntry reverse(Deal deal, BigDecimal amount, Long paymentRecordId) {
        return debitHeldCredit(deal, amount, paymentRecordId, WalletLedgerEntryType.REVERSAL,
            "Dispute resolved in buyer's favor for deal " + deal.getDealCode() + ", held credit reversed");
    }

    private WalletLedgerEntry debitHeldCredit(
        Deal deal, BigDecimal amount, Long paymentRecordId, WalletLedgerEntryType type, String reason
    ) {
        WalletAccount wallet = getOrCreateWallet(resolveSeller(deal).getId());
        wallet.debit(amount);
        walletAccountRepository.save(wallet);
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            type,
            amount.negate(),
            wallet.getBalance(),
            reason,
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * Buyer's payment has been verified — log a LOCK entry on the buyer's own wallet so they can
     * see those funds as tied up in escrow. Unlike {@link #hold}, this never touches the buyer's
     * balance: the money sits with Paystack/Ozow, not in Kammo's credit ledger for the buyer.
     */
    @Transactional
    public WalletLedgerEntry lockBuyerFunds(Deal deal, BigDecimal amount, Long paymentRecordId) {
        WalletAccount wallet = getOrCreateWallet(resolveBuyer(deal).getId());
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.LOCK,
            amount,
            wallet.getBalance(),
            "Payment secured for deal " + deal.getDealCode() + ", funds held in escrow pending delivery",
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * The buyer's escrowed funds for this deal are no longer locked — either paid out to the
     * seller, refunded, or reversed after a dispute. Logs an UNLOCK entry on the buyer's wallet;
     * does not touch their balance for the same reason {@link #lockBuyerFunds} doesn't.
     */
    @Transactional
    public WalletLedgerEntry unlockBuyerFunds(Deal deal, BigDecimal amount, Long paymentRecordId, String reason) {
        WalletAccount wallet = getOrCreateWallet(resolveBuyer(deal).getId());
        return walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.UNLOCK,
            amount.negate(),
            wallet.getBalance(),
            reason,
            deal.getId(),
            paymentRecordId
        ));
    }

    /**
     * Starts a wallet top-up — the buyer charging their own card to fund their own balance, not a
     * deal-bound charge, so it skips the OTP/OTT layer that guards deal payments.
     */
    @Transactional
    public TopupResponse initiateTopup(AppUser user, BigDecimal amount) {
        String reference = "TOPUP-" + UUID.randomUUID().toString().replace("-", "");
        PaymentResult result = paymentService.chargeDirect(user, amount, reference);
        if (result.status() == PaymentStatus.FAILED) {
            throw new IllegalArgumentException("Top-up failed: " + result.message());
        }
        if (result.status() == PaymentStatus.SUCCEEDED) {
            PaymentRecord record = paymentService.findLatestByReference(reference, PaymentRecordType.TOPUP);
            creditTopup(user, amount, record.getId(), reference);
        }
        return new TopupResponse(result.status(), result.checkoutUrl(), reference);
    }

    /**
     * Confirms a top-up after the buyer returns from the Paystack checkout. Idempotent: if this
     * reference was already credited (e.g. the app called verify twice), it just returns the
     * current wallet state instead of crediting again.
     */
    @Transactional
    public WalletResponse verifyTopup(AppUser user, String reference) {
        PaymentRecord pendingRecord = paymentService.findLatestByReference(reference, PaymentRecordType.TOPUP);
        if (!walletLedgerEntryRepository.existsByPaymentRecordId(pendingRecord.getId())) {
            PaymentResult result = paymentService.verifyDirectCharge(reference, pendingRecord.getAmount());
            if (result.status() != PaymentStatus.SUCCEEDED) {
                throw new IllegalArgumentException("Top-up could not be confirmed: " + result.message());
            }
            creditTopup(user, pendingRecord.getAmount(), pendingRecord.getId(), reference);
        }
        return getMyWallet(user);
    }

    private void creditTopup(AppUser user, BigDecimal amount, Long paymentRecordId, String reference) {
        WalletAccount wallet = getOrCreateWallet(user.getId());
        wallet.credit(amount);
        walletAccountRepository.save(wallet);
        walletLedgerEntryRepository.save(new WalletLedgerEntry(
            wallet.getId(),
            WalletLedgerEntryType.TOPUP,
            amount,
            wallet.getBalance(),
            "Wallet topped up via " + reference,
            null,
            paymentRecordId
        ));
    }

    public WalletAccount getWallet(AppUser user) {
        return getOrCreateWallet(user.getId());
    }

    public List<WalletLedgerEntry> getRecentEntries(WalletAccount wallet) {
        return walletLedgerEntryRepository.findByWalletAccountIdOrderByCreatedAtDesc(wallet.getId());
    }

    @Transactional
    public WalletResponse getMyWallet(AppUser user) {
        WalletAccount wallet = getOrCreateWallet(user.getId());
        return WalletResponse.from(wallet, getRecentEntries(wallet));
    }

    private AppUser resolveSeller(Deal deal) {
        if (deal.getOwnerRole() == DealRole.SELLER) {
            return userRepository.findById(deal.getOwnerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Seller account not found for deal " + deal.getDealCode()));
        }
        return userRepository.findByPhoneNumber(deal.getOtherPartyPhoneNumber())
            .orElseThrow(() -> new IllegalArgumentException("Seller account not found for deal " + deal.getDealCode()));
    }

    private AppUser resolveBuyer(Deal deal) {
        if (deal.getOwnerRole() == DealRole.BUYER) {
            return userRepository.findById(deal.getOwnerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Buyer account not found for deal " + deal.getDealCode()));
        }
        return userRepository.findByPhoneNumber(deal.getOtherPartyPhoneNumber())
            .orElseThrow(() -> new IllegalArgumentException("Buyer account not found for deal " + deal.getDealCode()));
    }
}
