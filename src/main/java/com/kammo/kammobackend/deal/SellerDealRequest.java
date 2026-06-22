package com.kammo.kammobackend.deal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SellerDealRequest(
    @NotBlank
    @Size(max = 160)
    String itemName,

    @NotNull
    @DecimalMin("1.00")
    BigDecimal price,

    @NotBlank
    @Size(max = 2000)
    String description,

    @NotBlank
    @Size(max = 30)
    String buyerPhoneNumber,

    @NotNull
    DeliveryMethod deliveryMethod,

    @NotNull
    Integer inspectionWindowHours
) {
}
