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
    REFUNDED,
    CANCELLED;

    /**
     * Maps this backend status onto the public 5-step payment tracker
     * (Initiated -> Paid -> Transit -> Inspect -> Released) advertised on
     * kammo.co.za. DISPUTED and REFUNDED/CANCELLED are exception states that
     * fall outside the happy-path tracker, so they map to their own
     * categories rather than one of the 5 steps.
     */
    public TrackerStep toTrackerStep() {
        return switch (this) {
            case CREATED, AWAITING_BUYER_PAYMENT, BUYER_ACCEPTED, SELLER_ACCEPTED -> TrackerStep.INITIATED;
            case PAYMENT_SECURED -> TrackerStep.PAID;
            case AWAITING_COLLECTION, IN_TRANSIT -> TrackerStep.TRANSIT;
            case DELIVERED -> TrackerStep.INSPECT;
            case COMPLETED -> TrackerStep.RELEASED;
            case DISPUTED -> TrackerStep.DISPUTED;
            case REFUNDED, CANCELLED -> TrackerStep.ENDED;
        };
    }
}
