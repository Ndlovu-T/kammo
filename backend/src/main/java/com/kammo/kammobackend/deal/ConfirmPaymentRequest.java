package com.kammo.kammobackend.deal;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
    @NotBlank
    String reference
) {
}
