package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealPricing;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Real Paystack integration. Card charges go through Paystack's hosted checkout, so
 * {@link #charge} returns PENDING with a checkoutUrl — the deal is only secured once the buyer
 * completes checkout and the app calls back through {@link #verifyCharge}.
 */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "paystack")
public class PaystackPaymentProvider implements PaymentProvider {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_MAP =
        new ParameterizedTypeReference<>() {
        };

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public PaystackPaymentProvider(
        RestClient.Builder restClientBuilder,
        @Value("${app.paystack.secret-key}") String secretKey,
        @Value("${app.paystack.base-url}") String baseUrl,
        UserRepository userRepository,
        PaymentRepository paymentRepository
    ) {
        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + secretKey)
            .build();
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentResult charge(Deal deal, AppUser payer) {
        String reference = "KAMMO-" + deal.getDealCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try {
            Map<String, Object> response = restClient.post()
                .uri("/transaction/initialize")
                .body(Map.of(
                    "email", payer.getEmail(),
                    "amount", toSubunits(DealPricing.totalToPay(deal)),
                    "currency", "ZAR",
                    "reference", reference
                ))
                .retrieve()
                .body(JSON_MAP);

            String authorizationUrl = stringField(dataOf(response), "authorization_url");
            if (authorizationUrl == null) {
                return new PaymentResult(PaymentStatus.FAILED, reference, "Paystack did not return a checkout URL");
            }
            return new PaymentResult(PaymentStatus.PENDING, reference, "Redirect the buyer to complete payment", authorizationUrl);
        } catch (RestClientException e) {
            return new PaymentResult(PaymentStatus.FAILED, reference, "Paystack initialize failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult verifyCharge(String providerReference) {
        try {
            Map<String, Object> response = restClient.get()
                .uri("/transaction/verify/{reference}", providerReference)
                .retrieve()
                .body(JSON_MAP);

            String status = stringField(dataOf(response), "status");
            if ("success".equals(status)) {
                return new PaymentResult(PaymentStatus.SUCCEEDED, providerReference, "Payment verified");
            }
            return new PaymentResult(PaymentStatus.FAILED, providerReference, "Paystack transaction status: " + status);
        } catch (RestClientException e) {
            return new PaymentResult(PaymentStatus.FAILED, providerReference, "Paystack verify failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult payout(Deal deal) {
        AppUser seller = resolveSeller(deal);
        if (seller == null) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Seller account not found for deal " + deal.getDealCode());
        }
        if (!seller.hasBankAccount()) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Seller has not added a bank account");
        }

        try {
            String recipientCode = seller.getPaystackRecipientCode();
            if (recipientCode == null) {
                recipientCode = createTransferRecipient(seller);
                seller.setPaystackRecipientCode(recipientCode);
                userRepository.save(seller);
            }

            Map<String, Object> response = restClient.post()
                .uri("/transfer")
                .body(Map.of(
                    "source", "balance",
                    "amount", toSubunits(deal.getPrice()),
                    "recipient", recipientCode,
                    "reason", "KAMMO payout for deal " + deal.getDealCode()
                ))
                .retrieve()
                .body(JSON_MAP);

            Map<String, Object> data = dataOf(response);
            String status = stringField(data, "status");
            String transferReference = stringField(data, "reference");
            return new PaymentResult(mapAsyncStatus(status), transferReference, "Paystack transfer status: " + status);
        } catch (RestClientException e) {
            return new PaymentResult(PaymentStatus.FAILED, null, "Paystack transfer failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refund(Deal deal) {
        Optional<PaymentRecord> chargeRecord = paymentRepository
            .findFirstByDealIdAndTypeAndStatusOrderByCreatedAtDesc(deal.getId(), PaymentRecordType.CHARGE, PaymentStatus.SUCCEEDED);
        if (chargeRecord.isEmpty()) {
            return new PaymentResult(PaymentStatus.FAILED, null, "No successful charge found to refund for this deal");
        }

        String originalReference = chargeRecord.get().getProviderReference();
        try {
            Map<String, Object> response = restClient.post()
                .uri("/refund")
                .body(Map.of(
                    "transaction", originalReference,
                    "amount", toSubunits(DealPricing.totalToPay(deal))
                ))
                .retrieve()
                .body(JSON_MAP);

            String status = stringField(dataOf(response), "status");
            return new PaymentResult(mapAsyncStatus(status), originalReference, "Paystack refund status: " + status);
        } catch (RestClientException e) {
            return new PaymentResult(PaymentStatus.FAILED, originalReference, "Paystack refund failed: " + e.getMessage());
        }
    }

    private String createTransferRecipient(AppUser seller) {
        Map<String, Object> response = restClient.post()
            .uri("/transferrecipient")
            .body(Map.of(
                "type", "basa",
                "name", seller.getBankAccountName(),
                "account_number", seller.getBankAccountNumber(),
                "bank_code", seller.getBankCode(),
                "currency", "ZAR"
            ))
            .retrieve()
            .body(JSON_MAP);

        String recipientCode = stringField(dataOf(response), "recipient_code");
        if (recipientCode == null) {
            throw new IllegalStateException("Paystack did not return a transfer recipient code");
        }
        return recipientCode;
    }

    private AppUser resolveSeller(Deal deal) {
        if (deal.getOwnerRole() == DealRole.SELLER) {
            return userRepository.findById(deal.getOwnerUserId()).orElse(null);
        }
        return userRepository.findByPhoneNumber(deal.getOtherPartyPhoneNumber()).orElse(null);
    }

    private PaymentStatus mapAsyncStatus(String status) {
        if ("success".equals(status)) {
            return PaymentStatus.SUCCEEDED;
        }
        if ("pending".equals(status) || "queued".equals(status) || "otp".equals(status)) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.FAILED;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> dataOf(Map<String, Object> response) {
        return response == null ? null : (Map<String, Object>) response.get("data");
    }

    private String stringField(Map<String, Object> map, String key) {
        return map == null ? null : (String) map.get(key);
    }

    private long toSubunits(BigDecimal rand) {
        return rand.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
