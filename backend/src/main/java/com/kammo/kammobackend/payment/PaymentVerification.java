package com.kammo.kammobackend.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One row per Kammo-issued OTP for a deal's payment. Holds the OTP (hashed) used to confirm the
 * buyer personally authorized this charge, and — once the OTP is verified — the signed, single-use
 * checkout reference (OTT) issued for the resulting Paystack/Ozow charge attempt.
 */
@Entity
@Table(name = "payment_verifications")
public class PaymentVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealId;

    @Column(nullable = false)
    private Long buyerUserId;

    @Column(nullable = false, length = 100)
    private String otpCodeHash;

    @Column(nullable = false)
    private Instant otpExpiresAt;

    @Column(nullable = false)
    private int otpAttempts = 0;

    @Column(nullable = false)
    private boolean otpVerified = false;

    private Instant otpVerifiedAt;

    @Column(length = 150)
    private String chargeReference;

    @Column(length = 100)
    private String ottToken;

    private Instant ottExpiresAt;

    @Column(nullable = false)
    private boolean ottConsumed = false;

    private Instant ottConsumedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentVerification() {
    }

    public PaymentVerification(Long dealId, Long buyerUserId, String otpCodeHash, Instant otpExpiresAt) {
        this.dealId = dealId;
        this.buyerUserId = buyerUserId;
        this.otpCodeHash = otpCodeHash;
        this.otpExpiresAt = otpExpiresAt;
    }

    public void incrementOtpAttempts() {
        this.otpAttempts++;
    }

    public void markOtpVerified() {
        this.otpVerified = true;
        this.otpVerifiedAt = Instant.now();
    }

    public void issueChargeReference(String chargeReference, String ottToken, Instant ottExpiresAt) {
        this.chargeReference = chargeReference;
        this.ottToken = ottToken;
        this.ottExpiresAt = ottExpiresAt;
    }

    public void consumeOtt() {
        this.ottConsumed = true;
        this.ottConsumedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDealId() {
        return dealId;
    }

    public Long getBuyerUserId() {
        return buyerUserId;
    }

    public String getOtpCodeHash() {
        return otpCodeHash;
    }

    public Instant getOtpExpiresAt() {
        return otpExpiresAt;
    }

    public int getOtpAttempts() {
        return otpAttempts;
    }

    public boolean isOtpVerified() {
        return otpVerified;
    }

    public Instant getOtpVerifiedAt() {
        return otpVerifiedAt;
    }

    public String getChargeReference() {
        return chargeReference;
    }

    public String getOttToken() {
        return ottToken;
    }

    public Instant getOttExpiresAt() {
        return ottExpiresAt;
    }

    public boolean isOttConsumed() {
        return ottConsumed;
    }

    public Instant getOttConsumedAt() {
        return ottConsumedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
