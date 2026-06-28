package com.kammo.kammobackend.deal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.delivery.DeliveryProvider;
import com.kammo.kammobackend.delivery.ShipmentResult;
import com.kammo.kammobackend.delivery.ShipmentStatus;
import com.kammo.kammobackend.message.CreateMessageRequest;
import com.kammo.kammobackend.message.DealMessage;
import com.kammo.kammobackend.message.DealMessageRepository;
import com.kammo.kammobackend.payment.PaymentResult;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.payment.PaymentStatus;
import com.kammo.kammobackend.payment.PaymentVerificationService;
import com.kammo.kammobackend.rating.CreateRatingRequest;
import com.kammo.kammobackend.rating.DealRating;
import com.kammo.kammobackend.rating.DealRatingRepository;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.wallet.WalletService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealCodeGenerator dealCodeGenerator;

    @Mock
    private DealMessageRepository messageRepository;

    @Mock
    private DealRatingRepository ratingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentVerificationService paymentVerificationService;

    @Mock
    private DeliveryProvider deliveryProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DealAuditService auditService;

    @Mock
    private WalletService walletService;

    private DealService dealService;

    private AppUser buyer;
    private AppUser seller;
    private AppUser stranger;

    @BeforeEach
    void setUp() {
        dealService = new DealService(
            dealRepository, dealCodeGenerator, messageRepository, ratingRepository, paymentService, paymentVerificationService,
            deliveryProvider, eventPublisher, auditService, walletService
        );

        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 1L);

        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
        ReflectionTestUtils.setField(seller, "id", 2L);

        stranger = new AppUser("KM0003", "0710000003", "stranger@example.com", "hashed");
        ReflectionTestUtils.setField(stranger, "id", 3L);
    }

    private Deal buyerCreatedDeal(DealStatus status, DeliveryMethod deliveryMethod) {
        Deal deal = new Deal(
            "DEAL0001",
            buyer.getId(),
            DealRole.BUYER,
            "Widget",
            new BigDecimal("100.00"),
            "A widget",
            seller.getPhoneNumber(),
            deliveryMethod,
            24,
            null,
            null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        deal.setStatus(status);
        return deal;
    }

    private CreateDealRequest createRequest(DeliveryMethod method, DealAddressRequest collection, DealAddressRequest delivery) {
        return new CreateDealRequest(
            DealRole.BUYER,
            "Widget",
            new BigDecimal("100.00"),
            "A widget",
            seller.getPhoneNumber(),
            method,
            24,
            collection,
            delivery
        );
    }

    private DealAddressRequest fullStreetAddress() {
        return new DealAddressRequest(
            "12 Main Road", null, "Cape Town", "Western Cape", "8001",
            null, null, "Jane Doe", "0710000009", null, null
        );
    }

    private DealAddressRequest lockerAddress() {
        return new DealAddressRequest(
            null, null, null, null, null,
            null, null, null, null, null, "CPT001"
        );
    }

    @Test
    void createDeal_meetupDoesNotRequireAddress() {
        when(dealCodeGenerator.generateUniqueDealCode()).thenReturn("DEAL0002");
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealResponse response = dealService.createDeal(buyer, createRequest(DeliveryMethod.MEETUP, null, null));

        assertThat(response.dealCode()).isEqualTo("DEAL0002");
        assertThat(response.collectionAddress()).isNull();
    }

    @Test
    void createDeal_courierWithoutAnyAddressThrows() {
        assertThatThrownBy(() -> dealService.createDeal(buyer, createRequest(DeliveryMethod.COURIER, null, null)))
            .isInstanceOf(IllegalArgumentException.class);
        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_courierWithIncompleteStreetAddressThrows() {
        DealAddressRequest incomplete = new DealAddressRequest(
            "12 Main Road", null, "Cape Town", null, null,
            null, null, "Jane Doe", "0710000009", null, null
        );

        assertThatThrownBy(() -> dealService.createDeal(buyer, createRequest(DeliveryMethod.COURIER, incomplete, incomplete)))
            .isInstanceOf(IllegalArgumentException.class);
        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_courierWithFullStreetAddressSucceeds() {
        when(dealCodeGenerator.generateUniqueDealCode()).thenReturn("DEAL0003");
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealResponse response = dealService.createDeal(
            buyer,
            createRequest(DeliveryMethod.COURIER, fullStreetAddress(), fullStreetAddress())
        );

        assertThat(response.collectionAddress()).isNotNull();
        assertThat(response.collectionAddress().city()).isEqualTo("Cape Town");
    }

    @Test
    void createDeal_pudoLockerWithTerminalIdOnlySucceeds() {
        when(dealCodeGenerator.generateUniqueDealCode()).thenReturn("DEAL0004");
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealResponse response = dealService.createDeal(
            buyer,
            createRequest(DeliveryMethod.PUDO_LOCKER, lockerAddress(), lockerAddress())
        );

        assertThat(response.deliveryAddress().lockerTerminalId()).isEqualTo("CPT001");
    }

    private void mockFindDeal(Deal deal) {
        when(dealRepository.findByDealCode(deal.getDealCode())).thenReturn(Optional.of(deal));
    }

    @Test
    void requestPaymentOtp_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.requestPaymentOtp(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentVerificationService, never()).requestOtp(any(), any());
    }

    @Test
    void requestPaymentOtp_sendsOtpForBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        Map<String, Object> response = dealService.requestPaymentOtp(buyer, deal.getDealCode());

        verify(paymentVerificationService).requestOtp(deal, buyer);
        assertThat(response).containsEntry("otpSent", true);
    }

    @Test
    void verifyPaymentOtp_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.verifyPaymentOtp(stranger, deal.getDealCode(), "123456"))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentVerificationService, never()).verifyOtp(any(), any(), any());
    }

    @Test
    void verifyPaymentOtp_verifiesCodeForBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        Map<String, Object> response = dealService.verifyPaymentOtp(buyer, deal.getDealCode(), "123456");

        verify(paymentVerificationService).verifyOtp(deal, buyer, "123456");
        assertThat(response).containsEntry("verified", true);
    }

    @Test
    void verifyPaymentOtp_propagatesIncorrectCodeFailure() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Incorrect OTP code"))
            .when(paymentVerificationService).verifyOtp(deal, buyer, "000000");

        assertThatThrownBy(() -> dealService.verifyPaymentOtp(buyer, deal.getDealCode(), "000000"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markPaymentSecured_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markPaymentSecured(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).charge(any(), any());
    }

    @Test
    void markPaymentSecured_rejectsWrongStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.CREATED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markPaymentSecured(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).charge(any(), any());
    }

    @Test
    void markPaymentSecured_chargesAndTransitionsOnSuccess() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.charge(deal, buyer)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));

        DealResponse response = dealService.markPaymentSecured(buyer, deal.getDealCode());

        verify(paymentService).charge(deal, buyer);
        assertThat(response.status()).isEqualTo(DealStatus.PAYMENT_SECURED);
        assertThat(response.trackerStep()).isEqualTo(TrackerStep.PAID);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.PAYMENT_SECURED);
        verify(eventPublisher).publishEvent(new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, buyer.getId(), DealRole.BUYER, seller.getPhoneNumber()
        ));
    }

    @Test
    void markPaymentSecured_acceptsFromBuyerAcceptedStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.BUYER_ACCEPTED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.charge(deal, buyer)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));

        DealResponse response = dealService.markPaymentSecured(buyer, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.PAYMENT_SECURED);
    }

    @Test
    void markPaymentSecured_acceptsFromSellerAcceptedStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.SELLER_ACCEPTED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.charge(deal, buyer)).thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));

        DealResponse response = dealService.markPaymentSecured(buyer, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.PAYMENT_SECURED);
    }

    @Test
    void markPaymentSecured_throwsAndLeavesStatusUnchangedWhenChargeFails() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.charge(deal, buyer)).thenReturn(new PaymentResult(PaymentStatus.FAILED, "ref-1", "declined"));

        assertThatThrownBy(() -> dealService.markPaymentSecured(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.AWAITING_BUYER_PAYMENT);
        verify(eventPublisher, never()).publishEvent(any(DealPaymentConfirmedEvent.class));
    }

    @Test
    void markPaymentSecured_returnsCheckoutUrlAndLeavesDealAwaitingWhenPaymentPending() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.charge(deal, buyer))
            .thenReturn(new PaymentResult(PaymentStatus.PENDING, "ref-1", "Redirect to checkout", "https://checkout.paystack.com/abc123"));

        DealResponse response = dealService.markPaymentSecured(buyer, deal.getDealCode());

        assertThat(response.pendingPaymentUrl()).isEqualTo("https://checkout.paystack.com/abc123");
        assertThat(response.status()).isEqualTo(DealStatus.AWAITING_BUYER_PAYMENT);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.AWAITING_BUYER_PAYMENT);
        verify(eventPublisher, never()).publishEvent(any(DealPaymentConfirmedEvent.class));
    }

    @Test
    void confirmPayment_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.confirmPayment(stranger, deal.getDealCode(), "ref-1"))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).verifyCharge(any(), any());
    }

    @Test
    void confirmPayment_securesPaymentWhenVerificationSucceeds() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.verifyCharge(deal, "ref-1"))
            .thenReturn(new PaymentResult(PaymentStatus.SUCCEEDED, "ref-1", "ok"));

        DealResponse response = dealService.confirmPayment(buyer, deal.getDealCode(), "ref-1");

        assertThat(response.status()).isEqualTo(DealStatus.PAYMENT_SECURED);
        verify(eventPublisher).publishEvent(new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, buyer.getId(), DealRole.BUYER, seller.getPhoneNumber()
        ));
    }

    @Test
    void confirmPayment_throwsWhenVerificationFails() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(paymentService.verifyCharge(deal, "ref-1"))
            .thenReturn(new PaymentResult(PaymentStatus.FAILED, "ref-1", "declined"));

        assertThatThrownBy(() -> dealService.confirmPayment(buyer, deal.getDealCode(), "ref-1"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.AWAITING_BUYER_PAYMENT);
    }

    @Test
    void markReadyForCollection_rejectsWrongStartingStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_BUYER_PAYMENT, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markReadyForCollection(seller, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markReadyForCollection_createsShipmentForCourierDeliveries() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.COURIER);
        mockFindDeal(deal);
        when(deliveryProvider.createShipment(deal))
            .thenReturn(new ShipmentResult(ShipmentStatus.SUCCEEDED, "PD-WAYBILL-2", "https://tracking.pudo.co.za/PD-WAYBILL-2", "ok"));

        DealResponse response = dealService.markReadyForCollection(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.AWAITING_COLLECTION);
        assertThat(deal.getWaybillNumber()).isEqualTo("PD-WAYBILL-2");
    }

    @Test
    void markReadyForCollection_skipsShipmentCreationForMeetup() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        DealResponse response = dealService.markReadyForCollection(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.AWAITING_COLLECTION);
        assertThat(deal.getWaybillNumber()).isNull();
        verify(deliveryProvider, never()).createShipment(any());
    }

    @Test
    void markInTransit_rejectsNonSeller() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_COLLECTION, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markInTransit(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markInTransit_rejectsWrongStartingStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markInTransit(seller, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markInTransit_transitionsOnSuccess() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_COLLECTION, DeliveryMethod.COURIER);
        mockFindDeal(deal);
        when(deliveryProvider.createShipment(deal))
            .thenReturn(new ShipmentResult(ShipmentStatus.SUCCEEDED, "PD-WAYBILL-1", "https://tracking.pudo.co.za/PD-WAYBILL-1", "ok"));

        DealResponse response = dealService.markInTransit(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.IN_TRANSIT);
        assertThat(deal.getWaybillNumber()).isEqualTo("PD-WAYBILL-1");
    }

    @Test
    void markInTransit_throwsWhenShipmentCreationFails() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_COLLECTION, DeliveryMethod.COURIER);
        mockFindDeal(deal);
        when(deliveryProvider.createShipment(deal))
            .thenReturn(new ShipmentResult(ShipmentStatus.FAILED, null, null, "PUDO is unavailable"));

        assertThatThrownBy(() -> dealService.markInTransit(seller, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(deal.getStatus()).isEqualTo(DealStatus.IN_TRANSIT);
    }

    @Test
    void markDelivered_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markDelivered(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markDelivered_rejectsWrongStartingStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.AWAITING_COLLECTION, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.markDelivered(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markDelivered_transitionsAndStampsDeliveredAt() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        DealResponse response = dealService.markDelivered(buyer, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.DELIVERED);
        assertThat(deal.getDeliveredAt()).isNotNull();
    }

    @Test
    void confirmDelivery_rejectsNonBuyer() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.confirmDelivery(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).payout(any());
    }

    @Test
    void confirmDelivery_rejectsWrongStartingStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.confirmDelivery(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).payout(any());
    }

    @Test
    void confirmDelivery_completesAndPaysOutOnSuccess() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        DealResponse response = dealService.confirmDelivery(buyer, deal.getDealCode());

        verify(paymentService).payout(deal);
        assertThat(response.status()).isEqualTo(DealStatus.COMPLETED);
    }

    @Test
    void confirmDelivery_succeedsFromDeliveredStatus() {
        Deal deal = buyerCreatedDeal(DealStatus.DELIVERED, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        DealResponse response = dealService.confirmDelivery(buyer, deal.getDealCode());

        verify(paymentService).payout(deal);
        assertThat(response.status()).isEqualTo(DealStatus.COMPLETED);
    }

    @Test
    void cancelDeal_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.CREATED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.cancelDeal(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelDeal_rejectsOncePaymentSecured() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.cancelDeal(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelDeal_succeedsAndPublishesEventWhenLinkedToListing() {
        Deal deal = buyerCreatedDeal(DealStatus.CREATED, DeliveryMethod.MEETUP);
        deal.setListingId(55L);
        mockFindDeal(deal);

        DealResponse response = dealService.cancelDeal(buyer, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.CANCELLED);
        verify(eventPublisher).publishEvent(new DealCancelledEvent(55L));
    }

    @Test
    void cancelDeal_succeedsWithoutEventWhenNotLinkedToListing() {
        Deal deal = buyerCreatedDeal(DealStatus.CREATED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        DealResponse response = dealService.cancelDeal(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.CANCELLED);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void raiseDispute_rejectsAfterInspectionWindowExpired() {
        Deal deal = buyerCreatedDeal(DealStatus.DELIVERED, DeliveryMethod.COURIER);
        deal.setDeliveredAt(java.time.Instant.now().minus(48, java.time.temporal.ChronoUnit.HOURS));
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.raiseDispute(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void raiseDispute_succeedsWithinInspectionWindow() {
        Deal deal = buyerCreatedDeal(DealStatus.DELIVERED, DeliveryMethod.COURIER);
        deal.setDeliveredAt(java.time.Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS));
        mockFindDeal(deal);

        DealResponse response = dealService.raiseDispute(buyer, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.DISPUTED);
    }

    @Test
    void raiseDispute_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.raiseDispute(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void raiseDispute_rejectsWhenCompleted() {
        Deal deal = buyerCreatedDeal(DealStatus.COMPLETED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.raiseDispute(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void raiseDispute_rejectsWhenAlreadyDisputed() {
        Deal deal = buyerCreatedDeal(DealStatus.DISPUTED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.raiseDispute(seller, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void raiseDispute_succeedsForParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        DealResponse response = dealService.raiseDispute(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.DISPUTED);
    }

    @Test
    void getTracking_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        deal.setWaybillNumber("PD-WAYBILL-3");
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.getTracking(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTracking_rejectsWhenNoShipmentExistsYet() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.COURIER);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.getTracking(buyer, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTracking_returnsResultForParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.COURIER);
        deal.setWaybillNumber("PD-WAYBILL-3");
        mockFindDeal(deal);
        com.kammo.kammobackend.delivery.TrackingResult trackingResult = new com.kammo.kammobackend.delivery.TrackingResult(
            "PD-WAYBILL-3", com.kammo.kammobackend.delivery.TrackingStatus.IN_TRANSIT, List.of()
        );
        when(deliveryProvider.trackShipment("PD-WAYBILL-3")).thenReturn(trackingResult);

        com.kammo.kammobackend.delivery.TrackingResult result = dealService.getTracking(buyer, deal.getDealCode());

        assertThat(result).isEqualTo(trackingResult);
    }

    @Test
    void getMessages_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.getMessages(stranger, deal.getDealCode()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getMessages_returnsMessagesForParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        DealMessage message = new DealMessage(deal.getId(), buyer.getId(), "hello");
        ReflectionTestUtils.setField(message, "id", 99L);
        when(messageRepository.findByDealIdOrderByCreatedAtAsc(deal.getId())).thenReturn(List.of(message));

        List<Map<String, Object>> messages = dealService.getMessages(buyer, deal.getDealCode());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).containsEntry("body", "hello");
    }

    @Test
    void sendMessage_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.sendMessage(stranger, deal.getDealCode(), new CreateMessageRequest("hi")))
            .isInstanceOf(IllegalArgumentException.class);
        verify(messageRepository, never()).save(any());
        verify(auditService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    void sendMessage_recordsAuditEvent() {
        Deal deal = buyerCreatedDeal(DealStatus.PAYMENT_SECURED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        DealMessage message = new DealMessage(deal.getId(), buyer.getId(), "hello");
        ReflectionTestUtils.setField(message, "id", 99L);
        when(messageRepository.save(any(DealMessage.class))).thenReturn(message);

        dealService.sendMessage(buyer, deal.getDealCode(), new CreateMessageRequest("hello"));

        verify(auditService).record(
            eq(deal.getId()),
            eq(com.kammo.kammobackend.audit.DealAuditEventType.MESSAGE_SENT),
            eq(buyer.getId()),
            any(),
            any()
        );
    }

    @Test
    void getAuditTrail_delegatesToAuditServiceUsingDealId() {
        Deal deal = buyerCreatedDeal(DealStatus.COMPLETED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        List<com.kammo.kammobackend.audit.DealAuditEventResponse> trail = List.of();
        when(auditService.getAuditTrail(deal.getId())).thenReturn(trail);

        List<com.kammo.kammobackend.audit.DealAuditEventResponse> result = dealService.getAuditTrail(deal.getDealCode());

        assertThat(result).isSameAs(trail);
    }

    @Test
    void rateDeal_rejectsNonParticipant() {
        Deal deal = buyerCreatedDeal(DealStatus.COMPLETED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.rateDeal(stranger, deal.getDealCode(), new CreateRatingRequest(5, "great")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rateDeal_rejectsWhenDealNotCompleted() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.rateDeal(buyer, deal.getDealCode(), new CreateRatingRequest(5, "great")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rateDeal_rejectsSecondRatingFromSameUser() {
        Deal deal = buyerCreatedDeal(DealStatus.COMPLETED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(ratingRepository.existsByDealIdAndRaterUserId(deal.getId(), buyer.getId())).thenReturn(true);

        assertThatThrownBy(() -> dealService.rateDeal(buyer, deal.getDealCode(), new CreateRatingRequest(5, "great")))
            .isInstanceOf(IllegalArgumentException.class);
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void rateDeal_savesRatingForParticipantOnCompletedDeal() {
        Deal deal = buyerCreatedDeal(DealStatus.COMPLETED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);
        when(ratingRepository.existsByDealIdAndRaterUserId(deal.getId(), buyer.getId())).thenReturn(false);
        DealRating savedRating = new DealRating(deal.getId(), buyer.getId(), 5, "great");
        when(ratingRepository.save(any(DealRating.class))).thenReturn(savedRating);

        Map<String, Object> response = dealService.rateDeal(buyer, deal.getDealCode(), new CreateRatingRequest(5, "great"));

        assertThat(response).containsEntry("score", 5);
    }

    @Test
    void resolveDispute_rejectsWhenNotDisputed() {
        Deal deal = buyerCreatedDeal(DealStatus.IN_TRANSIT, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        assertThatThrownBy(() -> dealService.resolveDispute(deal.getDealCode(), DisputeResolution.RELEASE_SELLER))
            .isInstanceOf(IllegalArgumentException.class);
        verify(paymentService, never()).payout(any());
        verify(paymentService, never()).refund(any());
    }

    @Test
    void resolveDispute_releaseSellerPaysOutAndCompletes() {
        Deal deal = buyerCreatedDeal(DealStatus.DISPUTED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        DealResponse response = dealService.resolveDispute(deal.getDealCode(), DisputeResolution.RELEASE_SELLER);

        verify(paymentService).payout(deal);
        verify(paymentService, never()).refund(any());
        assertThat(response.status()).isEqualTo(DealStatus.COMPLETED);
    }

    @Test
    void resolveDispute_refundBuyerRefundsAndSetsRefunded() {
        Deal deal = buyerCreatedDeal(DealStatus.DISPUTED, DeliveryMethod.MEETUP);
        mockFindDeal(deal);

        DealResponse response = dealService.resolveDispute(deal.getDealCode(), DisputeResolution.REFUND_BUYER);

        verify(paymentService).refund(deal);
        verify(paymentService, never()).payout(any());
        assertThat(response.status()).isEqualTo(DealStatus.REFUNDED);
    }

    @Test
    void getDisputedDeals_returnsMappedDisputedDeals() {
        Deal deal = buyerCreatedDeal(DealStatus.DISPUTED, DeliveryMethod.MEETUP);
        when(dealRepository.findByStatusOrderByCreatedAtDesc(DealStatus.DISPUTED)).thenReturn(List.of(deal));

        List<DealResponse> responses = dealService.getDisputedDeals();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).dealCode()).isEqualTo(deal.getDealCode());
        assertThat(responses.get(0).status()).isEqualTo(DealStatus.DISPUTED);
    }
}
