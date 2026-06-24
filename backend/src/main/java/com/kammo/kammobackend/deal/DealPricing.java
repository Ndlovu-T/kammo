package com.kammo.kammobackend.deal;

import java.math.BigDecimal;

public final class DealPricing {

    private static final BigDecimal KAMMO_FEE_RATE = new BigDecimal("0.01");
    private static final BigDecimal COURIER_FEE = new BigDecimal("120.00");

    private DealPricing() {
    }

    public static BigDecimal kammoFee(Deal deal) {
        return deal.getPrice().multiply(KAMMO_FEE_RATE);
    }

    public static BigDecimal courierFee(Deal deal) {
        return deal.getDeliveryMethod() != DeliveryMethod.MEETUP ? COURIER_FEE : BigDecimal.ZERO;
    }

    public static BigDecimal totalToPay(Deal deal) {
        return deal.getPrice().add(kammoFee(deal)).add(courierFee(deal));
    }
}
