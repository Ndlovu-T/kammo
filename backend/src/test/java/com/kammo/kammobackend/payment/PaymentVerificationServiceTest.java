package com.kammo.kammobackend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentVerificationServiceTest {

    @Mock
    private PaymentVerificationRepository repository;

    @Mock
    private SmsSender smsSender;

    @Mock
    private DealAuditService auditService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AtomicReference<PaymentVerification> stored = new AtomicReference<>();

    private PaymentVerificationService service;
    private Deal deal;
    private AppUser buyer;

    @BeforeEach
    void setUp() {
        service = new PaymentVerificationService(
            repository, smsSender, passwordEncoder, auditService, "test-ott-secret-at-least-32-characters-long"
        );

        deal = new Deal(
            "DEAL0099", 1L, DealRole.BUYER, "Widget", new BigDecimal("100.00"), "A widget",
            "0710000002", DeliveryMethod.MEETUP, 24, null, null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);

        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 1L);

        lenient().when(repository.save(any(PaymentVerification.class))).thenAnswer(invocation -> {
            PaymentVerification verification = invocation.getArgument(0);
            stored.set(verification);
            return verification;
        });
        lenient().when(repository.findFirstByDealIdOrderByCreatedAtDesc(deal.getId()))
            .thenAnswer(invocation -> Optional.ofNullable(stored.get()));
    }

    @Test
    void requestOtp_sendsSmsAndPersists() {
        service.requestOtp(deal, buyer);

        verify(smsSender).send(eq(buyer.getPhoneNumber()), any());
        assertThat(stored.get()).isNotNull();
        assertThat(stored.get().isOtpVerified()).isFalse();
    }

    @Test
    void requestOtp_rateLimitsAfterTooManyRequestsInWindow() {
        when(repository.countByDealIdAndCreatedAtAfter(eq(deal.getId()), any(Instant.class))).thenReturn(3L);

        assertThatThrownBy(() -> service.requestOtp(deal, buyer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Too many OTP requests");
    }

    @Test
    void verifyOtp_rejectsWrongCodeAndIncrementsAttempts() {
        service.requestOtp(deal, buyer);

        assertThatThrownBy(() -> service.verifyOtp(deal, buyer, "000000"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Incorrect OTP code");
        assertThat(stored.get().getOtpAttempts()).isEqualTo(1);
        assertThat(stored.get().isOtpVerified()).isFalse();
    }

    @Test
    void verifyOtp_rejectsExpiredCode() {
        service.requestOtp(deal, buyer);
        ReflectionTestUtils.setField(stored.get(), "otpExpiresAt", Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> service.verifyOtp(deal, buyer, "000000"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void verifyOtp_acceptsCorrectCode() {
        String code = capturedOtpCode();

        service.verifyOtp(deal, buyer, code);

        assertThat(stored.get().isOtpVerified()).isTrue();
    }

    @Test
    void requireVerifiedReference_throwsWhenOtpNotVerified() {
        service.requestOtp(deal, buyer);

        assertThatThrownBy(() -> service.requireVerifiedReference(deal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OTP verification is required");
    }

    @Test
    void requireVerifiedReference_issuesReferenceOnceVerified() {
        service.verifyOtp(deal, buyer, capturedOtpCode());

        String reference = service.requireVerifiedReference(deal);

        assertThat(reference).startsWith("KAMMO-DEAL0099-");
        assertThat(stored.get().getChargeReference()).isEqualTo(reference);
    }

    @Test
    void requireVerifiedReference_rejectsSecondIssueForSameOtp() {
        service.verifyOtp(deal, buyer, capturedOtpCode());
        service.requireVerifiedReference(deal);

        assertThatThrownBy(() -> service.requireVerifiedReference(deal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already been used");
    }

    @Test
    void validateAndConsumeReference_acceptsMatchingReferenceOnce() {
        service.verifyOtp(deal, buyer, capturedOtpCode());
        String reference = service.requireVerifiedReference(deal);

        service.validateAndConsumeReference(deal, reference);

        assertThat(stored.get().isOttConsumed()).isTrue();
    }

    @Test
    void validateAndConsumeReference_rejectsReplayOfConsumedReference() {
        service.verifyOtp(deal, buyer, capturedOtpCode());
        String reference = service.requireVerifiedReference(deal);
        service.validateAndConsumeReference(deal, reference);

        assertThatThrownBy(() -> service.validateAndConsumeReference(deal, reference))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateAndConsumeReference_rejectsForgedReference() {
        service.verifyOtp(deal, buyer, capturedOtpCode());
        service.requireVerifiedReference(deal);

        assertThatThrownBy(() -> service.validateAndConsumeReference(deal, "KAMMO-DEAL0099-forgedtoken.bogus"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validateAndConsumeReference_rejectsMismatchedDealReference() {
        service.verifyOtp(deal, buyer, capturedOtpCode());
        service.requireVerifiedReference(deal);

        assertThatThrownBy(() -> service.validateAndConsumeReference(deal, "KAMMO-DEAL9999-someothertoken.sig"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private String capturedOtpCode() {
        service.requestOtp(deal, buyer);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsSender, atLeastOnce()).send(eq(buyer.getPhoneNumber()), messageCaptor.capture());
        Matcher matcher = Pattern.compile("(\\d{6})").matcher(messageCaptor.getValue());
        if (!matcher.find()) {
            throw new IllegalStateException("OTP code not found in message: " + messageCaptor.getValue());
        }
        return matcher.group(1);
    }
}
