package com.kammo.kammobackend.deal;

public record DealStatusChangedEvent(
    Long dealId,
    String dealCode,
    String itemName,
    Long ownerUserId,
    DealRole ownerRole,
    String otherPartyPhoneNumber,
    DealStatus fromStatus,
    DealStatus toStatus
) {
}
