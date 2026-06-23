package com.kammo.kammobackend.deal;

public enum DealStatus {
    CREATED,
    AWAITING_BUYER_PAYMENT,
    BUYER_ACCEPTED,
    SELLER_ACCEPTED,
    PAYMENT_SECURED,
    AWAITING_COLLECTION,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    DISPUTED,
    REFUNDED
}
