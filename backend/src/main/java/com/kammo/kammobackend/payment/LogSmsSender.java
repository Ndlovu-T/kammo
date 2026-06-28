package com.kammo.kammobackend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stand-in SMS sender that logs instead of dispatching a real message. Swap in a real provider
 * (e.g. Twilio/Clickatell) behind an {@code app.sms.provider} property once credentials are
 * available, following the same conditional-bean pattern as {@link PaymentProvider}.
 */
@Component
public class LogSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

    @Override
    public void send(String phoneNumber, String message) {
        log.info("[SMS to {}] {}", phoneNumber, message);
    }
}
