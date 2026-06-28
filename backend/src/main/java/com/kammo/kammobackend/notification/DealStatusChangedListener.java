package com.kammo.kammobackend.notification;

import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealStatusChangedEvent;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fans out a WhatsApp template + email to both deal participants on every status change.
 * Runs async and after commit so a WhatsApp/email outage never blocks or rolls back the
 * underlying deal transaction; every send is independently try/caught and only logged on
 * failure. Missing contact info (no phone/email) is skipped rather than treated as an error.
 */
@Component
public class DealStatusChangedListener {

    private static final Logger log = LoggerFactory.getLogger(DealStatusChangedListener.class);

    private final WhatsAppClient whatsAppClient;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final String mailFrom;

    public DealStatusChangedListener(
        WhatsAppClient whatsAppClient,
        JavaMailSender mailSender,
        UserRepository userRepository,
        @Value("${app.mail.from}") String mailFrom
    ) {
        this.whatsAppClient = whatsAppClient;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.mailFrom = mailFrom;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDealStatusChanged(DealStatusChangedEvent event) {
        DealNotificationTemplate template = DealNotificationMatrix.forStatus(event.toStatus());
        if (template == null) {
            return;
        }

        AppUser owner = userRepository.findById(event.ownerUserId()).orElse(null);
        AppUser otherParty = userRepository.findByPhoneNumber(event.otherPartyPhoneNumber()).orElse(null);
        AppUser buyer = event.ownerRole() == DealRole.BUYER ? owner : otherParty;
        AppUser seller = event.ownerRole() == DealRole.SELLER ? owner : otherParty;

        notify(buyer, template.buyer(), event);
        notify(seller, template.seller(), event);
    }

    private void notify(AppUser recipient, DealNotificationMessage message, DealStatusChangedEvent event) {
        if (recipient == null) {
            log.warn("Could not resolve a recipient for deal {} status change to {}; skipping notification",
                event.dealCode(), event.toStatus());
            return;
        }
        sendWhatsApp(recipient, message, event);
        sendEmail(recipient, message, event);
    }

    private void sendWhatsApp(AppUser recipient, DealNotificationMessage message, DealStatusChangedEvent event) {
        if (recipient.getPhoneNumber() == null || recipient.getPhoneNumber().isBlank()) {
            return;
        }
        try {
            whatsAppClient.sendTemplate(
                recipient.getPhoneNumber(),
                message.whatsappTemplateName(),
                List.of(event.dealCode(), event.itemName())
            );
        } catch (Exception e) {
            log.error("WhatsApp notification failed for deal {} user {}: {}", event.dealCode(), recipient.getId(), e.getMessage(), e);
        }
    }

    private void sendEmail(AppUser recipient, DealNotificationMessage message, DealStatusChangedEvent event) {
        if (message.emailSubject() == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(mailFrom);
            mail.setTo(recipient.getEmail());
            mail.setSubject(message.emailSubject());
            mail.setText(message.renderEmailBody(event.dealCode(), event.itemName()));
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Email notification failed for deal {} user {}: {}", event.dealCode(), recipient.getId(), e.getMessage(), e);
        }
    }
}
