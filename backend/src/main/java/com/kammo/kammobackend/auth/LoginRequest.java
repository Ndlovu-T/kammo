package com.kammo.kammobackend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank
    @Size(max = 30)
    String phoneNumber,

    @NotBlank
    String password
) {
}
