package com.kammo.kammobackend.payment;

public record PaymentResult(PaymentStatus status, String providerReference, String message) {
}
