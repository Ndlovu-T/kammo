package com.kammo.kammobackend.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.deal.DealPaymentConfirmedEvent;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmedNotificationListenerTest {

    @Mock
    private PaymentNotifyClient paymentNotifyClient;

    @Mock
    private UserRepository userRepository;

    private PaymentConfirmedNotificationListener listener;

    private AppUser seller;

    @BeforeEach
    void setUp() {
        listener = new PaymentConfirmedNotificationListener(paymentNotifyClient, userRepository);
        seller = new AppUser("KM0002", "0710000002", "seller@example.com", "hashed");
    }

    @Test
    void resolvesSellerByPhoneWhenOwnerIsBuyer_andNotifiesWorker() {
        DealPaymentConfirmedEvent event = new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, 1L, DealRole.BUYER, "0710000002"
        );
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));

        listener.onPaymentConfirmed(event);

        verify(paymentNotifyClient).notifyPaymentConfirmed(
            "DEAL0001", 10L, "seller@example.com", "KM0002", "Widget", new BigDecimal("100.00"), 24
        );
    }

    @Test
    void resolvesSellerByOwnerIdWhenOwnerIsSeller() {
        DealPaymentConfirmedEvent event = new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, 2L, DealRole.SELLER, "0710000001"
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(seller));

        listener.onPaymentConfirmed(event);

        verify(paymentNotifyClient).notifyPaymentConfirmed(
            "DEAL0001", 10L, "seller@example.com", "KM0002", "Widget", new BigDecimal("100.00"), 24
        );
    }

    @Test
    void skipsNotificationWhenSellerCannotBeResolved() {
        DealPaymentConfirmedEvent event = new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, 1L, DealRole.BUYER, "0799999999"
        );
        when(userRepository.findByPhoneNumber("0799999999")).thenReturn(Optional.empty());

        listener.onPaymentConfirmed(event);

        verify(paymentNotifyClient, never()).notifyPaymentConfirmed(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void swallowsWorkerCallFailureWithoutThrowing() {
        DealPaymentConfirmedEvent event = new DealPaymentConfirmedEvent(
            10L, "DEAL0001", "Widget", new BigDecimal("100.00"), 24, 1L, DealRole.BUYER, "0710000002"
        );
        when(userRepository.findByPhoneNumber("0710000002")).thenReturn(Optional.of(seller));
        doThrow(new RuntimeException("Worker unreachable"))
            .when(paymentNotifyClient).notifyPaymentConfirmed(any(), any(), any(), any(), any(), any(), any());

        listener.onPaymentConfirmed(event);
    }
}
