package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.audit.DealAuditEventType;
import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.user.AppUser;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Kammo's own payment confirmation layer, independent of Paystack/Ozow's checkout auth (see
 * docs/compliance-tasks/07-otp-ott-verification.md):
 *
 * <ul>
 *   <li>OTP — a Kammo-generated code sent to the buyer's phone, confirming they personally
 *       authorized this specific charge, required before any checkout redirect is initiated.</li>
 *   <li>OTT — a signed, short-lived, single-use checkout reference issued only after the OTP is
 *       verified, so a forged or replayed Paystack/Ozow reference is rejected on confirmation.</li>
 * </ul>
 */
@Service
public class PaymentVerificationService {

    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private static final Duration OTP_REQUEST_WINDOW = Duration.ofMinutes(5);
    private static final int MAX_OTP_REQUESTS_PER_WINDOW = 3;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final Duration OTT_VALIDITY = Duration.ofMinutes(10);

    private final PaymentVerificationRepository repository;
    private final SmsSender smsSender;
    private final PasswordEncoder passwordEncoder;
    private final DealAuditService auditService;
    private final String ottSecret;
    private final SecureRandom random = new SecureRandom();

    public PaymentVerificationService(
        PaymentVerificationRepository repository,
        SmsSender smsSender,
        PasswordEncoder passwordEncoder,
        DealAuditService auditService,
        @Value("${app.payment.ott-secret}") String ottSecret
    ) {
        this.repository = repository;
        this.smsSender = smsSender;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.ottSecret = ottSecret;
    }

    public void requestOtp(Deal deal, AppUser buyer) {
        Instant windowStart = Instant.now().minus(OTP_REQUEST_WINDOW);
        if (repository.countByDealIdAndCreatedAtAfter(deal.getId(), windowStart) >= MAX_OTP_REQUESTS_PER_WINDOW) {
            throw new IllegalArgumentException("Too many OTP requests for this deal — please wait a few minutes and try again");
        }

        String code = generateOtpCode();
        PaymentVerification verification = new PaymentVerification(
            deal.getId(),
            buyer.getId(),
            passwordEncoder.encode(code),
            Instant.now().plus(OTP_VALIDITY)
        );
        repository.save(verification);

        smsSender.send(buyer.getPhoneNumber(), "Your Kammo payment verification code is " + code + ". It expires in 5 minutes.");
        auditService.record(
            deal.getId(),
            DealAuditEventType.PAYMENT_VERIFICATION,
            buyer.getId(),
            "Payment OTP requested",
            Map.of("event", "otp_requested")
        );
    }

    public void verifyOtp(Deal deal, AppUser buyer, String code) {
        PaymentVerification verification = latestVerification(deal);
        if (verification.isOtpVerified()) {
            return;
        }
        if (Instant.now().isAfter(verification.getOtpExpiresAt())) {
            throw new IllegalArgumentException("This OTP has expired — request a new one");
        }
        if (verification.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            throw new IllegalArgumentException("Too many incorrect attempts — request a new OTP");
        }
        if (!passwordEncoder.matches(code, verification.getOtpCodeHash())) {
            verification.incrementOtpAttempts();
            repository.save(verification);
            auditService.record(
                deal.getId(),
                DealAuditEventType.PAYMENT_VERIFICATION,
                buyer.getId(),
                "Payment OTP attempt failed",
                Map.of("event", "otp_failed")
            );
            throw new IllegalArgumentException("Incorrect OTP code");
        }

        verification.markOtpVerified();
        repository.save(verification);
        auditService.record(
            deal.getId(),
            DealAuditEventType.PAYMENT_VERIFICATION,
            buyer.getId(),
            "Payment OTP verified",
            Map.of("event", "otp_verified")
        );
    }

    /**
     * Called from {@link PaymentService#charge} before the checkout redirect is initiated. Issues
     * the signed OTT and the Paystack/Ozow charge reference it is embedded in; throws if the buyer
     * has not completed the OTP step for this deal.
     */
    public String requireVerifiedReference(Deal deal) {
        PaymentVerification verification = latestVerification(deal);
        if (!verification.isOtpVerified()) {
            throw new IllegalArgumentException("OTP verification is required before payment can proceed");
        }
        if (verification.getChargeReference() != null) {
            throw new IllegalArgumentException("This OTP has already been used to initiate a payment — request a new one");
        }

        Instant expiresAt = Instant.now().plus(OTT_VALIDITY);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String token = nonce + "." + sign(deal.getId(), nonce, expiresAt);
        String reference = "KAMMO-" + deal.getDealCode() + "-" + token;

        verification.issueChargeReference(reference, token, expiresAt);
        repository.save(verification);
        return reference;
    }

    /**
     * Called from {@link PaymentService#verifyCharge} before trusting the processor's status for a
     * reference. Rejects mismatched, forged, expired or already-consumed references.
     */
    public void validateAndConsumeReference(Deal deal, String providerReference) {
        PaymentVerification verification = latestVerification(deal);
        if (verification.getChargeReference() == null || verification.isOttConsumed()) {
            throw new IllegalArgumentException("No active payment session found for this deal — possible tampering or replay");
        }
        if (!verification.getChargeReference().equals(providerReference)) {
            throw new IllegalArgumentException("Payment reference does not match the verified checkout session — possible tampering or replay");
        }
        if (Instant.now().isAfter(verification.getOttExpiresAt())) {
            throw new IllegalArgumentException("The checkout session for this payment has expired");
        }
        if (!signatureValid(deal, verification.getOttToken(), verification.getOttExpiresAt())) {
            throw new IllegalArgumentException("Payment token signature is invalid — possible forgery");
        }

        verification.consumeOtt();
        repository.save(verification);
    }

    private PaymentVerification latestVerification(Deal deal) {
        return repository.findFirstByDealIdOrderByCreatedAtDesc(deal.getId())
            .orElseThrow(() -> new IllegalArgumentException("No OTP has been requested for this deal yet"));
    }

    private String generateOtpCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private boolean signatureValid(Deal deal, String token, Instant expiresAt) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            return false;
        }
        String expectedSignature = sign(deal.getId(), parts[0], expiresAt);
        return MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            parts[1].getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sign(Long dealId, String nonce, Instant expiresAt) {
        String payload = dealId + ":" + nonce + ":" + expiresAt.getEpochSecond();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(ottSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign one-time payment token", e);
        }
    }
}
