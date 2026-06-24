package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.user.AppUser;

public interface PaymentProvider {

    PaymentResult charge(Deal deal, AppUser payer);

    PaymentResult verifyCharge(String providerReference);

    PaymentResult payout(Deal deal);

    PaymentResult refund(Deal deal);
}
