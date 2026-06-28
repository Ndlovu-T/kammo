package com.kammo.kammobackend.notification;

import java.util.List;

/**
 * WhatsApp Business API requires pre-approved message templates for business-initiated
 * messages outside a 24h customer service window, so callers pass a template name plus
 * positional body parameters rather than free-form text.
 */
public interface WhatsAppClient {

    void sendTemplate(String toPhoneNumber, String templateName, List<String> bodyParameters);
}
