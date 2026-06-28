package com.kammo.kammobackend.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DealStatusChangedEvent;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class DealStatusChangedListenerTest {

    @Mock
    private WhatsAppClient whatsAppClient;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    private DealStatusChangedListener listener;

    private AppUser buyer;
    private AppUser seller;

    @BeforeEach
    void setUp() {
        listener = new DealStatusChangedListener(whatsAppClient, mailSender, userRepository, "noreply@kammo.co.za");
        buyer = new AppUser("KM0001", "0710000001", "buyer@example.com", "hashed");
        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
        org.springframework.test.util.ReflectionTestUtils.setField(buyer, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(seller, "id", 2L);
    }

    private DealStatusChangedEvent eventFor(DealStatus toStatus) {
        return new DealStatusChangedEvent(
            10L, "DEAL0001", "Widget", 1L, DealRole.BUYER, "0710000002", DealStatus.CREATED, toStatus
        );
    }

    @ParameterizedTest
    @EnumSource(value = DealStatus.class, names = {
        "SELLER_ACCEPTED", "PAYMENT_SECURED", "IN_TRANSIT", "DELIVERED", "COMPLETED"
    })
    void notifiesBothPartiesViaWhatsAppForCanonicalTransitions(DealStatus toStatus) {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onDealStatusChanged(eventFor(toStatus));

        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> paramsCaptor = ArgumentCaptor.forClass(List.class);
        verify(whatsAppClient, times(2)).sendTemplate(anyString(), templateCaptor.capture(), paramsCaptor.capture());

        assertThat(templateCaptor.getAllValues()).allSatisfy(name -> assertThat(name).isNotBlank());
        assertThat(paramsCaptor.getAllValues()).allSatisfy(
            params -> assertThat(params).containsExactly("DEAL0001", "Widget")
        );
    }

    @Test
    void sendsEmailWithDealCodeAndItemNameInBodyToBuyer() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onDealStatusChanged(eventFor(DealStatus.IN_TRANSIT));

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(mailCaptor.capture());
        SimpleMailMessage buyerMail = mailCaptor.getAllValues().stream()
            .filter(mail -> "buyer@example.com".equals(mail.getTo()[0]))
            .findFirst()
            .orElseThrow();
        assertThat(buyerMail.getText()).contains("DEAL0001").contains("Widget");
    }

    @Test
    void skipsEmailForSellerOnPaymentSecuredToAvoidDuplicateWithPaymentWorker() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onDealStatusChanged(eventFor(DealStatus.PAYMENT_SECURED));

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(whatsAppClient, times(2)).sendTemplate(anyString(), anyString(), any());
    }

    @Test
    void skipsRecipientThatCannotBeResolvedWithoutThrowing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onDealStatusChanged(eventFor(DealStatus.COMPLETED));

        verify(whatsAppClient, times(1)).sendTemplate(anyString(), anyString(), any());
    }

    @Test
    void skipsWhatsAppWhenRecipientHasNoPhoneNumberButStillSendsEmail() {
        AppUser noPhoneBuyer = new AppUser("KM0003", "", "nophone@example.com", "hashed");
        org.springframework.test.util.ReflectionTestUtils.setField(noPhoneBuyer, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(noPhoneBuyer));
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onDealStatusChanged(eventFor(DealStatus.IN_TRANSIT));

        verify(whatsAppClient, times(1)).sendTemplate(anyString(), anyString(), any());
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void swallowsWhatsAppFailureWithoutThrowingAndStillSendsEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));
        doThrow(new RuntimeException("WhatsApp API unreachable"))
            .when(whatsAppClient).sendTemplate(anyString(), anyString(), any());

        listener.onDealStatusChanged(eventFor(DealStatus.IN_TRANSIT));

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void doesNothingForStatusWithoutAConfiguredTemplate() {
        listener.onDealStatusChanged(eventFor(DealStatus.AWAITING_COLLECTION));

        verify(userRepository, never()).findById(any());
        verify(whatsAppClient, never()).sendTemplate(any(), any(), any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
