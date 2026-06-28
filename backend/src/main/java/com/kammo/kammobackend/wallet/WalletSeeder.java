package com.kammo.kammobackend.wallet;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealCodeGenerator;
import com.kammo.kammobackend.deal.DealRepository;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.KammoIdGenerator;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local-dev convenience: seeds a few deals at different lifecycle stages between the first
 * registered user and a synthetic counterparty, driving them through the real
 * {@link WalletService} methods so {@code GET /api/wallet/me} returns realistic, non-zero
 * balances and ledger history instead of an empty wallet, even when only one real account has
 * signed up locally. No-ops once any wallet ledger entry already exists.
 */
@Component
public class WalletSeeder implements ApplicationRunner {

    private static final String DEMO_COUNTERPARTY_PHONE = "+27000000001";
    private static final String DEMO_COUNTERPARTY_EMAIL = "demo.counterparty@kammo.local";

    private final WalletLedgerEntryRepository walletLedgerEntryRepository;
    private final WalletService walletService;
    private final DealRepository dealRepository;
    private final DealCodeGenerator dealCodeGenerator;
    private final UserRepository userRepository;
    private final KammoIdGenerator kammoIdGenerator;
    private final PasswordEncoder passwordEncoder;

    public WalletSeeder(
        WalletLedgerEntryRepository walletLedgerEntryRepository,
        WalletService walletService,
        DealRepository dealRepository,
        DealCodeGenerator dealCodeGenerator,
        UserRepository userRepository,
        KammoIdGenerator kammoIdGenerator,
        PasswordEncoder passwordEncoder
    ) {
        this.walletLedgerEntryRepository = walletLedgerEntryRepository;
        this.walletService = walletService;
        this.dealRepository = dealRepository;
        this.dealCodeGenerator = dealCodeGenerator;
        this.userRepository = userRepository;
        this.kammoIdGenerator = kammoIdGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (walletLedgerEntryRepository.count() != 0) {
            return;
        }

        List<AppUser> users = userRepository.findAll();
        AppUser seller = users.stream()
            .filter(candidate -> !candidate.getPhoneNumber().equals(DEMO_COUNTERPARTY_PHONE))
            .findFirst()
            .orElse(null);
        if (seller == null) {
            return;
        }

        AppUser buyer = userRepository.findByPhoneNumber(DEMO_COUNTERPARTY_PHONE)
            .orElseGet(this::createDemoCounterparty);

        seedCompletedDeal(seller, buyer);
        seedInEscrowDeal(seller, buyer);
        seedRefundedDeal(seller, buyer);
    }

    private AppUser createDemoCounterparty() {
        return userRepository.save(new AppUser(
            kammoIdGenerator.generateUniqueKammoId(),
            DEMO_COUNTERPARTY_PHONE,
            DEMO_COUNTERPARTY_EMAIL,
            passwordEncoder.encode("demo-counterparty-not-for-login")
        ));
    }

    private void seedCompletedDeal(AppUser seller, AppUser buyer) {
        Deal deal = saveDeal(seller, buyer, "MacBook Air M2", new BigDecimal("16500.00"), DealStatus.COMPLETED);
        walletService.hold(deal, deal.getPrice(), null);
        walletService.lockBuyerFunds(deal, deal.getPrice(), null);
        walletService.release(deal, deal.getPrice(), null);
        walletService.recordPayout(deal, deal.getPrice(), null);
        walletService.unlockBuyerFunds(deal, deal.getPrice(), null,
            "Paystack/Ozow payout completed for deal " + deal.getDealCode());
    }

    private void seedInEscrowDeal(AppUser seller, AppUser buyer) {
        Deal deal = saveDeal(seller, buyer, "Sony A7 IV", new BigDecimal("22000.00"), DealStatus.PAYMENT_SECURED);
        walletService.hold(deal, deal.getPrice(), null);
        walletService.lockBuyerFunds(deal, deal.getPrice(), null);
    }

    private void seedRefundedDeal(AppUser seller, AppUser buyer) {
        Deal deal = saveDeal(seller, buyer, "PS5 Bundle", new BigDecimal("12000.00"), DealStatus.REFUNDED);
        walletService.hold(deal, deal.getPrice(), null);
        walletService.lockBuyerFunds(deal, deal.getPrice(), null);
        walletService.refund(deal, deal.getPrice(), null);
        walletService.unlockBuyerFunds(deal, deal.getPrice(), null,
            "Buyer refunded for deal " + deal.getDealCode() + ", escrow released");
    }

    private Deal saveDeal(AppUser seller, AppUser buyer, String itemName, BigDecimal price, DealStatus status) {
        Deal deal = new Deal(
            dealCodeGenerator.generateUniqueDealCode(),
            seller.getId(),
            DealRole.SELLER,
            itemName,
            price,
            "Demo deal seeded for local wallet testing.",
            buyer.getPhoneNumber(),
            DeliveryMethod.MEETUP,
            48,
            null,
            null
        );
        deal.setStatus(status);
        return dealRepository.save(deal);
    }
}
