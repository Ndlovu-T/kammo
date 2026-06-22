package com.kammo.kammobackend.auth;

public record AuthResponse(
    String token,
    String tokenType,
    String kammoId,
    String phoneNumber,
    String email
) {
    public AuthResponse(String token, String kammoId, String phoneNumber, String email) {
        this(token, "Bearer", kammoId, phoneNumber, email);
    }
}
