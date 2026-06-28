package com.kammo.kammobackend.audit;

public enum DealAuditEventType {
    STATUS_CHANGED,
    PAYMENT_ACTION,
    PAYMENT_VERIFICATION,
    DELIVERY_UPDATE,
    MESSAGE_SENT,
    DISPUTE_OPENED,
    DISPUTE_RESOLVED
}
