package com.kammo.kammobackend.delivery;

import java.math.BigDecimal;

public record DeliveryRateQuote(String serviceName, BigDecimal fee, int estimatedBusinessDays) {
}
