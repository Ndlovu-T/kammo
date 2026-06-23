package com.kammo.kammobackend.marketplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateListingRequest(
    @NotBlank
    @Size(max = 60)
    String category,

    @NotBlank
    @Size(max = 160)
    String itemName,

    @NotNull
    @DecimalMin("1.00")
    BigDecimal price,

    @Size(max = 120)
    String location,

    @NotBlank
    @Size(max = 2000)
    String description
) {
}
