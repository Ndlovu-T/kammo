package com.kammo.kammobackend.deal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.message.CreateMessageRequest;
import com.kammo.kammobackend.message.DealMessage;
import com.kammo.kammobackend.message.DealMessageRepository;
import com.kammo.kammobackend.payment.PaymentResult;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.payment.PaymentStatus;
import com.kammo.kammobackend.rating.CreateRatingRequest;
import com.kammo.kammobackend.rating.DealRating;
import com.kammo.kammobackend.rating.DealRatingRepository;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private DealService dealService;

    private AppUser buyer;
    private AppUser seller;
    private AppUser stranger;

    @BeforeEach
    void setUp() {
        dealService = new DealService(dealRepository, dealCodeGenerator, messageRepository, ratingRepository, paymentService);

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
            24
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        deal.setStatus(status);
        return deal;
    }

    private void mockFindDeal(Deal deal) {
        when(dealRepository.findByDealCode(deal.getDealCode())).thenReturn(Optional.of(deal));
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
        assertThat(deal.getStatus()).isEqualTo(DealStatus.PAYMENT_SECURED);
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

        DealResponse response = dealService.markInTransit(seller, deal.getDealCode());

        assertThat(response.status()).isEqualTo(DealStatus.IN_TRANSIT);
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
