package com.kammo.kammobackend.payment;

public record PaymentResult(PaymentStatus status, String providerReference, String message, String checkoutUrl) {

    public PaymentResult(PaymentStatus status, String providerReference, String message) {
        this(status, providerReference, message, null);
    }
}
