package com.kammo.kammobackend.deal;

import java.math.BigDecimal;
import java.time.Instant;

public record DealResponse(
    String dealCode,
    String dealReference,
    String dealLink,
    DealRole ownerRole,
    String itemName,
    BigDecimal price,
    BigDecimal kammoFee,
    BigDecimal courierFee,
    BigDecimal totalToPay,
    String description,
    String otherPartyPhoneNumber,
    String nextBuyerAction,
    String nextSellerAction,
    DeliveryMethod deliveryMethod,
    Integer inspectionWindowHours,
    DealStatus status,
    String waybillNumber,
    Instant createdAt,
    Instant updatedAt
) {
    public static DealResponse from(Deal deal) {
        BigDecimal kammoFee = deal.getPrice().multiply(new BigDecimal("0.01"));
        BigDecimal courierFee = deal.getDeliveryMethod() == DeliveryMethod.COURIER ? new BigDecimal("120.00") : BigDecimal.ZERO;
        return new DealResponse(
            deal.getDealCode(),
            "KM-" + deal.getDealCode(),
            "https://kammo.co.za/deal/" + deal.getDealCode(),
            deal.getOwnerRole(),
            deal.getItemName(),
            deal.getPrice(),
            kammoFee,
            courierFee,
            deal.getPrice().add(kammoFee).add(courierFee),
            deal.getDescription(),
            deal.getOtherPartyPhoneNumber(),
            nextBuyerAction(deal),
            nextSellerAction(deal),
            deal.getDeliveryMethod(),
            deal.getInspectionWindowHours(),
            deal.getStatus(),
            deal.getWaybillNumber(),
            deal.getCreatedAt(),
            deal.getUpdatedAt()
        );
    }

    private static String nextBuyerAction(Deal deal) {
        return switch (deal.getStatus()) {
            case AWAITING_BUYER_PAYMENT -> "Review the seller's deal request and pay securely.";
            case BUYER_ACCEPTED -> "Pay securely to activate the deal.";
            case SELLER_ACCEPTED, CREATED -> "Wait for the seller to accept and prepare the item.";
            case PAYMENT_SECURED, AWAITING_COLLECTION, IN_TRANSIT, DELIVERED -> "Track delivery and confirm once received.";
            case COMPLETED -> "Rate the seller.";
            case DISPUTED -> "Follow the dispute process.";
        };
    }

    private static String nextSellerAction(Deal deal) {
        return switch (deal.getStatus()) {
            case CREATED -> "Accept the buyer-created deal.";
            case AWAITING_BUYER_PAYMENT -> "Share the deal link with the buyer and wait for payment.";
            case BUYER_ACCEPTED -> "Buyer accepted the deal. Wait for payment confirmation.";
            case SELLER_ACCEPTED -> "Wait for buyer payment.";
            case PAYMENT_SECURED -> "Prepare the item for collection or meetup.";
            case AWAITING_COLLECTION -> "Drop off the item or wait for courier collection.";
            case IN_TRANSIT, DELIVERED -> "Wait for buyer confirmation.";
            case COMPLETED -> "Payout can be released.";
            case DISPUTED -> "Respond to the dispute.";
        };
    }
}
