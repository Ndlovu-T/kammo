package com.kammo.kammobackend.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TopupRequest(
    @NotNull
    @DecimalMin("1.00")
    BigDecimal amount
) {
}
