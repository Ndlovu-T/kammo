package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult charge(Deal deal, AppUser payer, String reference) {
        return new PaymentResult(PaymentStatus.SUCCEEDED, reference, "Mock charge succeeded");
    }

    @Override
    public PaymentResult chargeDirect(AppUser payer, BigDecimal amount, String reference) {
        return new PaymentResult(PaymentStatus.SUCCEEDED, reference, "Mock top-up succeeded");
    }

    @Override
    public PaymentResult verifyCharge(String providerReference) {
        return new PaymentResult(PaymentStatus.SUCCEEDED, providerReference, "Mock charge already confirmed");
    }

    @Override
    public PaymentResult payout(Deal deal) {
        return new PaymentResult(PaymentStatus.SUCCEEDED, "MOCK-" + UUID.randomUUID(), "Mock payout succeeded");
    }

    @Override
    public PaymentResult refund(Deal deal) {
        return new PaymentResult(PaymentStatus.SUCCEEDED, "MOCK-" + UUID.randomUUID(), "Mock refund succeeded");
    }
}
