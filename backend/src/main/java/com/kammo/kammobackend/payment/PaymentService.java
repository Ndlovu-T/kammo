package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealPricing;
import com.kammo.kammobackend.user.AppUser;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentProvider paymentProvider;
    private final PaymentRepository paymentRepository;
    private final String providerName;

    public PaymentService(PaymentProvider paymentProvider, PaymentRepository paymentRepository) {
        this.paymentProvider = paymentProvider;
        this.paymentRepository = paymentRepository;
        this.providerName = paymentProvider.getClass().getSimpleName();
    }

    public PaymentResult charge(Deal deal, AppUser payer) {
        PaymentResult result = paymentProvider.charge(deal, payer);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.CHARGE,
            providerName,
            result.providerReference(),
            result.status(),
            DealPricing.totalToPay(deal)
        ));
        return result;
    }

    public PaymentResult verifyCharge(Deal deal, String providerReference) {
        PaymentResult result = paymentProvider.verifyCharge(providerReference);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.CHARGE,
            providerName,
            result.providerReference(),
            result.status(),
            DealPricing.totalToPay(deal)
        ));
        return result;
    }

    public PaymentResult payout(Deal deal) {
        PaymentResult result = paymentProvider.payout(deal);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.PAYOUT,
            providerName,
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
            providerName,
            result.providerReference(),
            result.status(),
            DealPricing.totalToPay(deal)
        ));
        return result;
    }
}
