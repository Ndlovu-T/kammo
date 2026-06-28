package com.kammo.kammobackend.notification;

import com.kammo.kammobackend.deal.DealPaymentConfirmedEvent;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fires the seller "payment confirmed, please ship" email via the Cloudflare Worker once a
 * deal's PAYMENT_SECURED transition has committed. Runs async and after commit so a Worker
 * outage never blocks or rolls back the payment confirmation itself; failures are only logged.
 */
@Component
public class PaymentConfirmedNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfirmedNotificationListener.class);

    private final PaymentNotifyClient paymentNotifyClient;
    private final UserRepository userRepository;

    public PaymentConfirmedNotificationListener(PaymentNotifyClient paymentNotifyClient, UserRepository userRepository) {
        this.paymentNotifyClient = paymentNotifyClient;
        this.userRepository = userRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentConfirmed(DealPaymentConfirmedEvent event) {
        AppUser seller = resolveSeller(event);
        if (seller == null) {
            log.warn("Could not resolve seller for deal {}; skipping payment-confirmed notification", event.dealCode());
            return;
        }

        try {
            paymentNotifyClient.notifyPaymentConfirmed(
                event.dealCode(),
                event.dealId(),
                seller.getEmail(),
                seller.getKammoId(),
                event.itemName(),
                event.price(),
                event.inspectionWindowHours()
            );
        } catch (Exception e) {
            log.error("Payment-confirmed Worker notification failed for deal {}: {}", event.dealCode(), e.getMessage(), e);
        }
    }

    private AppUser resolveSeller(DealPaymentConfirmedEvent event) {
        if (event.ownerRole() == DealRole.SELLER) {
            return userRepository.findById(event.ownerUserId()).orElse(null);
        }
        return userRepository.findByPhoneNumber(event.otherPartyPhoneNumber()).orElse(null);
    }
}
