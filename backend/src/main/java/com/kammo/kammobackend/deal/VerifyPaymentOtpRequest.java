package com.kammo.kammobackend.deal;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentOtpRequest(
    @NotBlank
    String code
) {
}
