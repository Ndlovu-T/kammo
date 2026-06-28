package com.kammo.kammobackend.wallet;

import com.kammo.kammobackend.payment.PaymentStatus;

public record TopupResponse(PaymentStatus status, String checkoutUrl, String reference) {
}
