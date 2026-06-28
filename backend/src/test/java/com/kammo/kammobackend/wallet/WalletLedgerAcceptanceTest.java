package com.kammo.kammobackend.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealCodeGenerator;
import com.kammo.kammobackend.deal.DealRepository;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealService;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.deal.DisputeResolution;
import com.kammo.kammobackend.delivery.DeliveryProvider;
import com.kammo.kammobackend.message.DealMessageRepository;
import com.kammo.kammobackend.payment.PaymentProvider;
import com.kammo.kammobackend.payment.PaymentRepository;
import com.kammo.kammobackend.payment.PaymentResult;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.payment.PaymentStatus;
import com.kammo.kammobackend.payment.PaymentVerificationService;
import com.kammo.kammobackend.rating.DealRatingRepository;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * End-to-end acceptance test for the Kamo Wallet ledger (compliance task 1):
 * charge succeeds -> wallet HOLD entry created with correct amount -> buyer confirms ->
 * RELEASE + PAYOUT entries created -> wallet balance returns to the pre-hold value for that
 * deal -> a Paystack transfer was invoked exactly once with the correct seller destination.
 *
 * <p>This wires real {@link WalletService} and {@link PaymentService} instances (only the
 * outermost {@link PaymentProvider} and the JPA repositories are mocked) so the actual balance
 * arithmetic and ledger bookkeeping are exercised, not just verified via mock interactions.
 */
@ExtendWith(MockitoExtension.class)
class WalletLedgerAcceptanceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealCodeGenerator dealCodeGenerator;

    @Mock
    private DealMessageRepository messageRepository;

    @Mock
    private DealRatingRepository ratingRepository;

    @Mock
    private DeliveryProvider deliveryProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DealAuditService auditService;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentVerificationService paymentVerificationService;

    @Mock
    private WalletAccountRepository walletAccountRepository;

    @Mock
    private WalletLedgerEntryRepository walletLedgerEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Deal> payoutDealCaptor;

    private DealService dealService;

    private AppUser buyer;
    private AppUser seller;

    @BeforeEach
    void setUp() {
        PaymentService paymentService = new PaymentService(paymentProvider, paymentRepository, paymentVerificationService, auditService);
        WalletService walletService = new WalletService(walletAccountRepository, walletLedgerEntryRepository, userRepository, paymentService);

        dealService = new DealService(
            dealRepository, dealCodeGenerator, messageRepository, ratingRepository, paymentService, paymentVerificationService,
            deliveryProvider, eventPublisher, auditService, walletService
        );

        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 1L);

        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
        ReflectionTestUtils.setField(seller, "id", 2L);
        seller.updateBankAccount("632005", "1234567890", "Seller Name");

        // In-memory stand-in for the wallet_accounts table: findByUserId/save round-trip a single
        // mutable WalletAccount instance, matching how a real JpaRepository would persist it.
        Map<Long, WalletAccount> walletsByUserId = new HashMap<>();
        when(walletAccountRepository.findByUserId(any())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            return Optional.ofNullable(walletsByUserId.get(userId));
        });
        when(walletAccountRepository.save(any(WalletAccount.class))).thenAnswer(invocation -> {
            WalletAccount wallet = invocation.getArgument(0);
            if (wallet.getId() == null) {
                ReflectionTestUtils.setField(wallet, "id", 55L);
            }
            walletsByUserId.put(wallet.getUserId(), wallet);
            return wallet;
        });
        when(walletLedgerEntryRepository.save(any(WalletLedgerEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Deal buyerCreatedDeal(BigDecimal price, DealStatus status) {
        Deal deal = new Deal(
            "DEAL0001",
            buyer.getId(),
            DealRole.BUYER,
            "Widget",
            price,
            "A widget",
            seller.getPhoneNumber(),
            DeliveryMethod.MEETUP,
            24,
            null,
            null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        deal.setStatus(status);
        when(dealRepository.findByDealCode(deal.getDealCode())).thenReturn(Optional.of(deal));
        return deal;
    }

    @Test
    void fullEscrowLifecycle_holdsReleasesPaysOutAndReconciliesWalletBalance() {
        BigDecimal price = new BigDecimal("100.00");
        Deal deal = buyerCreatedDeal(price, DealStatus.AWAITING_BUYER_PAYMENT);

        // 1. Charge succeeds.
        when(paymentVerificationService.requireVerifiedReference(deal)).thenReturn("ref-1");
        when(paymentProvider.charge(deal, buyer, "ref-1"))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));
        when(userRepository.findByPhoneNumber(seller.getPhoneNumber())).thenReturn(Optional.of(seller));
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));

        dealService.markPaymentSecured(buyer, deal.getDealCode());

        // 2. Wallet HOLD entry created with the correct amount, seller's wallet credited.
        WalletAccount sellerWallet = walletAccountRepository.findByUserId(seller.getId()).orElseThrow();
        assertThat(sellerWallet.getBalance()).isEqualByComparingTo(price);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.PAYMENT_SECURED);

        // 3. Buyer confirms delivery.
        deal.setStatus(DealStatus.IN_TRANSIT);
        when(paymentProvider.payout(deal))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "trf-1", "transfer queued"));

        dealService.confirmDelivery(buyer, deal.getDealCode());

        // 4. RELEASE + PAYOUT entries created, and the wallet balance returns to the pre-hold
        // value (zero) for this deal, since the held credit has now actually left the processor's
        // balance via the Paystack transfer.
        assertThat(deal.getStatus()).isEqualTo(DealStatus.COMPLETED);
        assertThat(sellerWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        ArgumentCaptor<WalletLedgerEntry> entryCaptor = ArgumentCaptor.forClass(WalletLedgerEntry.class);
        verify(walletLedgerEntryRepository, times(5)).save(entryCaptor.capture());
        var entries = entryCaptor.getAllValues();
        assertThat(entries.get(0).getType()).isEqualTo(WalletLedgerEntryType.HOLD);
        assertThat(entries.get(0).getAmount()).isEqualByComparingTo(price);
        assertThat(entries.get(1).getType()).isEqualTo(WalletLedgerEntryType.LOCK);
        assertThat(entries.get(1).getAmount()).isEqualByComparingTo(price);
        assertThat(entries.get(2).getType()).isEqualTo(WalletLedgerEntryType.RELEASE);
        assertThat(entries.get(3).getType()).isEqualTo(WalletLedgerEntryType.UNLOCK);
        assertThat(entries.get(3).getAmount()).isEqualByComparingTo(price.negate());
        assertThat(entries.get(4).getType()).isEqualTo(WalletLedgerEntryType.PAYOUT);
        assertThat(entries.get(4).getAmount()).isEqualByComparingTo(price.negate());

        // The buyer's own wallet never had its balance touched — escrowed funds sit with the
        // processor, not in Kammo's credit ledger for the buyer.
        WalletAccount buyerWallet = walletAccountRepository.findByUserId(buyer.getId()).orElseThrow();
        assertThat(buyerWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        // 5. The Paystack transfer was invoked exactly once with the correct seller destination.
        verify(paymentProvider, times(1)).payout(payoutDealCaptor.capture());
        Deal payoutDeal = payoutDealCaptor.getValue();
        assertThat(payoutDeal.getOtherPartyPhoneNumber()).isEqualTo(seller.getPhoneNumber());
        verify(paymentProvider, never()).refund(any());
    }

    @Test
    void disputeResolvedForBuyer_reversesHeldCreditInsteadOfPayingOut() {
        BigDecimal price = new BigDecimal("75.00");
        Deal deal = buyerCreatedDeal(price, DealStatus.AWAITING_BUYER_PAYMENT);

        when(paymentVerificationService.requireVerifiedReference(deal)).thenReturn("ref-1");
        when(paymentProvider.charge(deal, buyer, "ref-1"))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));
        when(userRepository.findByPhoneNumber(seller.getPhoneNumber())).thenReturn(Optional.of(seller));
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        dealService.markPaymentSecured(buyer, deal.getDealCode());

        WalletAccount sellerWallet = walletAccountRepository.findByUserId(seller.getId()).orElseThrow();
        assertThat(sellerWallet.getBalance()).isEqualByComparingTo(price);

        deal.setStatus(DealStatus.DISPUTED);
        when(paymentProvider.refund(deal)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "refunded"));

        dealService.resolveDispute(deal.getDealCode(), DisputeResolution.REFUND_BUYER);

        assertThat(deal.getStatus()).isEqualTo(DealStatus.REFUNDED);
        assertThat(sellerWallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(paymentProvider, never()).payout(any());
    }
}
