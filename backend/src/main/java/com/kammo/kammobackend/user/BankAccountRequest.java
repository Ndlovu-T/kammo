package com.kammo.kammobackend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BankAccountRequest(
    @NotBlank
    @Pattern(regexp = "\\d{3,10}", message = "Bank code must be the numeric Paystack bank code")
    String bankCode,

    @NotBlank
    @Pattern(regexp = "\\d{5,20}", message = "Account number must be numeric")
    String bankAccountNumber,

    @NotBlank
    @Size(max = 100)
    String bankAccountName
) {
}
