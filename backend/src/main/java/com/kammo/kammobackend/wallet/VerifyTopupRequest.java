package com.kammo.kammobackend.wallet;

import jakarta.validation.constraints.NotBlank;

public record VerifyTopupRequest(
    @NotBlank
    String reference
) {
}
