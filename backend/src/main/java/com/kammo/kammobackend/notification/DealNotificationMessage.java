package com.kammo.kammobackend.notification;

/**
 * One recipient's notification for a deal status change: a pre-approved WhatsApp template name
 * plus an email subject/body. The email fields may be null to skip email for that recipient
 * (e.g. PAYMENT_SECURED seller email is already sent by {@link PaymentConfirmedNotificationListener}).
 */
public record DealNotificationMessage(String whatsappTemplateName, String emailSubject, String emailBodyFormat) {

    public String renderEmailBody(String dealCode, String itemName) {
        return String.format(emailBodyFormat, dealCode, itemName);
    }
}
