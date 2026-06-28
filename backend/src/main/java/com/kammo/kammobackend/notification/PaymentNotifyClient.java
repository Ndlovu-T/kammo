package com.kammo.kammobackend.notification;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls the Cloudflare Worker (workers/payment-notify) that emails the seller once a deal's
 * payment is confirmed. See {@link PaymentConfirmedNotificationListener} for the caller.
 */
@Component
public class PaymentNotifyClient {

    private final RestClient restClient;
    private final String sharedSecret;

    public PaymentNotifyClient(
        RestClient.Builder restClientBuilder,
        @Value("${app.worker.payment-notify-url}") String paymentNotifyUrl,
        @Value("${app.worker.shared-secret}") String sharedSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(paymentNotifyUrl).build();
        this.sharedSecret = sharedSecret;
    }

    public void notifyPaymentConfirmed(
        String dealCode,
        Long dealId,
        String sellerEmail,
        String sellerName,
        String itemName,
        BigDecimal price,
        Integer inspectionWindowHours
    ) {
        restClient.post()
            .header("X-Kammo-Worker-Secret", sharedSecret)
            .body(new PaymentConfirmedNotifyRequest(
                dealCode, sellerEmail, sellerName, itemName, price, dealId, inspectionWindowHours
            ))
            .retrieve()
            .toBodilessEntity();
    }

    private record PaymentConfirmedNotifyRequest(
        String dealCode,
        String sellerEmail,
        String sellerName,
        String itemName,
        BigDecimal price,
        Long dealId,
        Integer inspectionWindowHours
    ) {
    }
}
