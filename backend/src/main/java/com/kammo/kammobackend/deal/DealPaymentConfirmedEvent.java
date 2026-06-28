package com.kammo.kammobackend.deal;

import java.math.BigDecimal;

public record DealPaymentConfirmedEvent(
    Long dealId,
    String dealCode,
    String itemName,
    BigDecimal price,
    Integer inspectionWindowHours,
    Long ownerUserId,
    DealRole ownerRole,
    String otherPartyPhoneNumber
) {
}
