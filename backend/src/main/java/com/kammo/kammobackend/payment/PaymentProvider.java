package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;

public interface PaymentProvider {

    PaymentResult charge(Deal deal, AppUser payer, String reference);

    /**
     * Charges a fixed amount not tied to a deal — used for wallet top-ups, where the buyer is
     * funding their own balance rather than authorizing a charge someone else initiated.
     */
    PaymentResult chargeDirect(AppUser payer, BigDecimal amount, String reference);

    PaymentResult verifyCharge(String providerReference);

    PaymentResult payout(Deal deal);

    PaymentResult refund(Deal deal);
}
