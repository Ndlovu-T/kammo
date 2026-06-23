package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentProvider paymentProvider;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentProvider paymentProvider, PaymentRepository paymentRepository) {
        this.paymentProvider = paymentProvider;
        this.paymentRepository = paymentRepository;
    }

    public PaymentResult charge(Deal deal, AppUser payer) {
        PaymentResult result = paymentProvider.charge(deal, payer);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.CHARGE,
            "mock",
            result.providerReference(),
            result.status(),
            totalToPay(deal)
        ));
        return result;
    }

    public PaymentResult payout(Deal deal) {
        PaymentResult result = paymentProvider.payout(deal);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.PAYOUT,
            "mock",
            result.providerReference(),
            result.status(),
            deal.getPrice()
        ));
        return result;
    }

    public PaymentResult refund(Deal deal) {
        PaymentResult result = paymentProvider.refund(deal);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.REFUND,
            "mock",
            result.providerReference(),
            result.status(),
            totalToPay(deal)
        ));
        return result;
    }

    private BigDecimal totalToPay(Deal deal) {
        BigDecimal kammoFee = deal.getPrice().multiply(new BigDecimal("0.01"));
        BigDecimal courierFee = deal.getDeliveryMethod() == DeliveryMethod.COURIER ? new BigDecimal("120.00") : BigDecimal.ZERO;
        return deal.getPrice().add(kammoFee).add(courierFee);
    }
}
