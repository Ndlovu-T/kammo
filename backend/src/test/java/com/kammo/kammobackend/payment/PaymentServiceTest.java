package com.kammo.kammobackend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentVerificationService paymentVerificationService;

    @Mock
    private DealAuditService auditService;

    @Captor
    private ArgumentCaptor<PaymentRecord> paymentRecordCaptor;

    private PaymentService paymentService;

    private AppUser buyer;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentProvider, paymentRepository, paymentVerificationService, auditService);
        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 1L);
    }

    private Deal dealWith(BigDecimal price, DeliveryMethod deliveryMethod) {
        Deal deal = new Deal(
            "DEAL0001",
            1L,
            DealRole.BUYER,
            "Widget",
            price,
            "A widget",
            "0710000002",
            deliveryMethod,
            24,
            null,
            null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        return deal;
    }

    @Test
    void charge_failsWithoutVerifiedOtpAndNeverReachesProvider() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.MEETUP);
        when(paymentVerificationService.requireVerifiedReference(deal))
            .thenThrow(new IllegalArgumentException("OTP verification is required before payment can proceed"));

        assertThatThrownBy(() -> paymentService.charge(deal, buyer))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("OTP verification is required");
        verify(paymentProvider, never()).charge(any(), any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void charge_passesKammoIssuedReferenceToProviderAndPersistsTotalToPayIncludingFeeAndCourierFee() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.COURIER);
        when(paymentVerificationService.requireVerifiedReference(deal)).thenReturn("KAMMO-DEAL0001-abc.sig");
        when(paymentProvider.charge(deal, buyer, "KAMMO-DEAL0001-abc.sig"))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "KAMMO-DEAL0001-abc.sig", "ok"));

        paymentService.charge(deal, buyer);

        verify(paymentProvider).charge(deal, buyer, "KAMMO-DEAL0001-abc.sig");
        verifySavedRecordMatches(PaymentRecordType.CHARGE, new BigDecimal("221.00"));
    }

    @Test
    void charge_persistsTotalToPayWithoutCourierFeeForMeetup() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.MEETUP);
        when(paymentVerificationService.requireVerifiedReference(deal)).thenReturn("KAMMO-DEAL0001-abc.sig");
        when(paymentProvider.charge(deal, buyer, "KAMMO-DEAL0001-abc.sig"))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "KAMMO-DEAL0001-abc.sig", "ok"));

        paymentService.charge(deal, buyer);

        verifySavedRecordMatches(PaymentRecordType.CHARGE, new BigDecimal("101.00"));
    }

    @Test
    void verifyCharge_rejectsReferenceTamperingBeforeCallingProvider() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.MEETUP);
        org.mockito.Mockito.doThrow(new IllegalArgumentException(
                "Payment reference does not match the verified checkout session — possible tampering or replay"
            ))
            .when(paymentVerificationService).validateAndConsumeReference(deal, "forged-ref");

        assertThatThrownBy(() -> paymentService.verifyCharge(deal, "forged-ref"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("tampering");
        verify(paymentProvider, never()).verifyCharge(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void verifyCharge_persistsResultAsChargeRecordOnceReferenceValidated() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.MEETUP);
        when(paymentProvider.verifyCharge("ref-1")).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));

        paymentService.verifyCharge(deal, "ref-1");

        verify(paymentVerificationService).validateAndConsumeReference(deal, "ref-1");
        verifySavedRecordMatches(PaymentRecordType.CHARGE, new BigDecimal("101.00"));
    }

    @Test
    void payout_persistsPriceOnlyWithNoFees() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.COURIER);
        when(paymentProvider.payout(deal)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-2", "ok"));

        paymentService.payout(deal);

        verifySavedRecordMatches(PaymentRecordType.PAYOUT, new BigDecimal("100.00"));
    }

    @Test
    void refund_persistsChargeTotal() {
        Deal deal = dealWith(new BigDecimal("100.00"), DeliveryMethod.COURIER);
        when(paymentProvider.refund(deal)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-3", "ok"));

        paymentService.refund(deal);

        verifySavedRecordMatches(PaymentRecordType.REFUND, new BigDecimal("221.00"));
    }

    private void verifySavedRecordMatches(PaymentRecordType expectedType, BigDecimal expectedAmount) {
        verify(paymentRepository).save(paymentRecordCaptor.capture());
        PaymentRecord saved = paymentRecordCaptor.getValue();
        assertThat(saved.getType()).isEqualTo(expectedType);
        assertThat(saved.getAmount()).isEqualByComparingTo(expectedAmount);
    }
}
