package com.kammo.kammobackend.wallet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletAccountRepository walletAccountRepository;

    @Mock
    private WalletLedgerEntryRepository walletLedgerEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    private WalletService walletService;

    private AppUser seller;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletAccountRepository, walletLedgerEntryRepository, userRepository, paymentService);

        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
        ReflectionTestUtils.setField(seller, "id", 2L);

        // Repositories behave like real JpaRepositories for save() — return what's passed in.
        // lenient() because not every test in this class triggers both saves (e.g. release() only
        // logs a ledger entry without saving the wallet, and getMyWallet() never saves at all).
        lenient().when(walletAccountRepository.save(any(WalletAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(walletLedgerEntryRepository.save(any(WalletLedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Deal sellerCreatedDeal(BigDecimal price) {
        Deal deal = new Deal(
            "DEAL0001",
            seller.getId(),
            DealRole.SELLER,
            "Widget",
            price,
            "A widget",
            "0710000001",
            DeliveryMethod.MEETUP,
            24,
            null,
            null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        return deal;
    }

    private WalletAccount existingWallet(Long userId, BigDecimal balance) {
        WalletAccount wallet = new WalletAccount(userId);
        ReflectionTestUtils.setField(wallet, "id", 99L);
        ReflectionTestUtils.setField(wallet, "balance", balance);
        return wallet;
    }

    @Test
    void hold_createsWalletIfMissingAndCreditsBalance() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.empty());

        WalletLedgerEntry entry = walletService.hold(deal, new BigDecimal("100.00"), null);

        assertThat(entry.getType()).isEqualTo(WalletLedgerEntryType.HOLD);
        assertThat(entry.getAmount()).isEqualByComparingTo("100.00");
        assertThat(entry.getBalanceAfter()).isEqualByComparingTo("100.00");
        assertThat(entry.getDealId()).isEqualTo(deal.getId());
    }

    @Test
    void hold_creditsExistingWalletBalance() {
        Deal deal = sellerCreatedDeal(new BigDecimal("50.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("20.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        WalletLedgerEntry entry = walletService.hold(deal, new BigDecimal("50.00"), null);

        assertThat(entry.getBalanceAfter()).isEqualByComparingTo("70.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("70.00");
    }

    @Test
    void release_doesNotChangeBalanceButLogsEntry() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("100.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        WalletLedgerEntry entry = walletService.release(deal, new BigDecimal("100.00"), null);

        assertThat(entry.getType()).isEqualTo(WalletLedgerEntryType.RELEASE);
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void recordPayout_debitsBalanceToZero() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("100.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        WalletLedgerEntry entry = walletService.recordPayout(deal, new BigDecimal("100.00"), null);

        assertThat(entry.getType()).isEqualTo(WalletLedgerEntryType.PAYOUT);
        assertThat(entry.getAmount()).isEqualByComparingTo("-100.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void recordPayout_throwsWhenItWouldMakeBalanceNegative() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("40.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.recordPayout(deal, new BigDecimal("100.00"), null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(wallet.getBalance()).isEqualByComparingTo("40.00");
    }

    @Test
    void reverse_debitsHeldCreditForDisputeResolvedInBuyersFavor() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("100.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        WalletLedgerEntry entry = walletService.reverse(deal, new BigDecimal("100.00"), null);

        assertThat(entry.getType()).isEqualTo(WalletLedgerEntryType.REVERSAL);
        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void refund_debitsHeldCreditWhenBuyerIsRefunded() {
        Deal deal = sellerCreatedDeal(new BigDecimal("100.00"));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("100.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));

        WalletLedgerEntry entry = walletService.refund(deal, new BigDecimal("100.00"), null);

        assertThat(entry.getType()).isEqualTo(WalletLedgerEntryType.REFUND);
        assertThat(wallet.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void getMyWallet_returnsBalanceAndRecentEntries() {
        WalletAccount wallet = existingWallet(seller.getId(), new BigDecimal("100.00"));
        when(walletAccountRepository.findByUserId(seller.getId())).thenReturn(Optional.of(wallet));
        WalletLedgerEntry entry = new WalletLedgerEntry(
            wallet.getId(), WalletLedgerEntryType.HOLD, new BigDecimal("100.00"), new BigDecimal("100.00"),
            "Funds secured", 10L, null
        );
        when(walletLedgerEntryRepository.findByWalletAccountIdOrderByCreatedAtDesc(wallet.getId()))
            .thenReturn(java.util.List.of(entry));

        WalletResponse response = walletService.getMyWallet(seller);

        assertThat(response.balance()).isEqualByComparingTo("100.00");
        assertThat(response.recentEntries()).hasSize(1);
        assertThat(response.recentEntries().get(0).type()).isEqualTo(WalletLedgerEntryType.HOLD);
    }
}
