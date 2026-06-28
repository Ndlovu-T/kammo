package com.kammo.kammobackend.notification;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Sends WhatsApp template messages via Meta's WhatsApp Business Cloud API
 * (https://developers.facebook.com/docs/whatsapp/cloud-api). Disabled by default so deals
 * keep working before a WhatsApp Business account and approved templates are provisioned.
 */
@Component
public class WhatsAppCloudApiClient implements WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudApiClient.class);

    private final RestClient restClient;
    private final String phoneNumberId;
    private final boolean enabled;

    public WhatsAppCloudApiClient(
        RestClient.Builder restClientBuilder,
        @Value("${app.whatsapp.base-url}") String baseUrl,
        @Value("${app.whatsapp.access-token}") String accessToken,
        @Value("${app.whatsapp.phone-number-id}") String phoneNumberId,
        @Value("${app.whatsapp.enabled}") boolean enabled
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + accessToken)
            .build();
        this.phoneNumberId = phoneNumberId;
        this.enabled = enabled;
    }

    @Override
    public void sendTemplate(String toPhoneNumber, String templateName, List<String> bodyParameters) {
        if (!enabled) {
            log.info("WhatsApp sending is disabled; skipping template {} to {}", templateName, toPhoneNumber);
            return;
        }

        List<Map<String, String>> parameters = bodyParameters.stream()
            .map(value -> Map.of("type", "text", "text", value))
            .toList();

        Map<String, Object> payload = Map.of(
            "messaging_product", "whatsapp",
            "to", toPhoneNumber,
            "type", "template",
            "template", Map.of(
                "name", templateName,
                "language", Map.of("code", "en"),
                "components", List.of(Map.of("type", "body", "parameters", parameters))
            )
        );

        restClient.post()
            .uri("/{phoneNumberId}/messages", phoneNumberId)
            .body(payload)
            .retrieve()
            .toBodilessEntity();
    }
}
