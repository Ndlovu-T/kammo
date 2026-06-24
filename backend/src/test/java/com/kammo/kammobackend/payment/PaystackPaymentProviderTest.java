package com.kammo.kammobackend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class PaystackPaymentProviderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private MockRestServiceServer mockServer;
    private PaystackPaymentProvider provider;

    private AppUser buyer;
    private AppUser seller;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        provider = new PaystackPaymentProvider(
            builder, "sk_test_secret", "https://api.paystack.co", userRepository, paymentRepository
        );

        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 1L);

        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
        ReflectionTestUtils.setField(seller, "id", 2L);
    }

    private Deal dealWith(DeliveryMethod deliveryMethod) {
        Deal deal = new Deal(
            "DEAL0001", buyer.getId(), DealRole.BUYER, "Widget", new BigDecimal("100.00"),
            "A widget", seller.getPhoneNumber(), deliveryMethod, 24, null, null
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        return deal;
    }

    @Test
    void charge_returnsPendingWithCheckoutUrlOnSuccessfulInitialize() {
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        mockServer.expect(requestTo("https://api.paystack.co/transaction/initialize"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("""
                {"status": true, "data": {"authorization_url": "https://checkout.paystack.com/abc123", "reference": "ref-1"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.charge(deal, buyer);

        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.checkoutUrl()).isEqualTo("https://checkout.paystack.com/abc123");
        mockServer.verify();
    }

    @Test
    void charge_returnsFailedWhenNoAuthorizationUrlReturned() {
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        mockServer.expect(requestTo("https://api.paystack.co/transaction/initialize"))
            .andRespond(withSuccess("""
                {"status": false, "data": null}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.charge(deal, buyer);

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void verifyCharge_returnsSucceededWhenStatusIsSuccess() {
        mockServer.expect(requestTo("https://api.paystack.co/transaction/verify/ref-1"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"status": "success"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.verifyCharge("ref-1");

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void verifyCharge_returnsFailedWhenStatusIsNotSuccess() {
        mockServer.expect(requestTo("https://api.paystack.co/transaction/verify/ref-1"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"status": "abandoned"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.verifyCharge("ref-1");

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void payout_failsWhenSellerHasNoBankAccount() {
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        when(userRepository.findByPhoneNumber(seller.getPhoneNumber())).thenReturn(Optional.of(seller));

        PaymentResult result = provider.payout(deal);

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.message()).contains("bank account");
    }

    @Test
    void payout_createsRecipientAndTransfersWhenSellerHasBankAccount() {
        seller.updateBankAccount("632005", "1234567890", "Seller Name");
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        when(userRepository.findByPhoneNumber(seller.getPhoneNumber())).thenReturn(Optional.of(seller));

        mockServer.expect(requestTo("https://api.paystack.co/transferrecipient"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"recipient_code": "RCP_123"}}
                """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://api.paystack.co/transfer"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"status": "success", "reference": "trf-1"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.payout(deal);

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(seller.getPaystackRecipientCode()).isEqualTo("RCP_123");
        verify(userRepository).save(seller);
        mockServer.verify();
    }

    @Test
    void payout_reusesCachedRecipientCode() {
        seller.updateBankAccount("632005", "1234567890", "Seller Name");
        seller.setPaystackRecipientCode("RCP_CACHED");
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        when(userRepository.findByPhoneNumber(seller.getPhoneNumber())).thenReturn(Optional.of(seller));

        mockServer.expect(requestTo("https://api.paystack.co/transfer"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"status": "success", "reference": "trf-2"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.payout(deal);

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(userRepository, never()).save(any());
        mockServer.verify();
    }

    @Test
    void refund_failsWhenNoSuccessfulChargeRecordExists() {
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        when(paymentRepository.findFirstByDealIdAndTypeAndStatusOrderByCreatedAtDesc(
            deal.getId(), PaymentRecordType.CHARGE, PaymentStatus.SUCCEEDED
        )).thenReturn(Optional.empty());

        PaymentResult result = provider.refund(deal);

        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void refund_succeedsUsingOriginalChargeReference() {
        Deal deal = dealWith(DeliveryMethod.MEETUP);
        PaymentRecord chargeRecord = new PaymentRecord(
            deal.getId(), PaymentRecordType.CHARGE, "PaystackPaymentProvider", "ref-1", PaymentStatus.SUCCEEDED, new BigDecimal("101.00")
        );
        when(paymentRepository.findFirstByDealIdAndTypeAndStatusOrderByCreatedAtDesc(
            deal.getId(), PaymentRecordType.CHARGE, PaymentStatus.SUCCEEDED
        )).thenReturn(Optional.of(chargeRecord));

        mockServer.expect(requestTo("https://api.paystack.co/refund"))
            .andRespond(withSuccess("""
                {"status": true, "data": {"status": "success"}}
                """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.refund(deal);

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(result.providerReference()).isEqualTo("ref-1");
    }
}
