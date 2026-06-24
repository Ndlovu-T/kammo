package com.kammo.kammobackend.user;

public record BankAccountResponse(
    boolean configured,
    String bankCode,
    String maskedAccountNumber,
    String bankAccountName
) {
    public static BankAccountResponse from(AppUser user) {
        if (!user.hasBankAccount()) {
            return new BankAccountResponse(false, null, null, null);
        }
        return new BankAccountResponse(
            true,
            user.getBankCode(),
            mask(user.getBankAccountNumber()),
            user.getBankAccountName()
        );
    }

    private static String mask(String accountNumber) {
        if (accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
}
