package com.kammo.kammobackend.payment;

import com.kammo.kammobackend.audit.DealAuditEventType;
import com.kammo.kammobackend.audit.DealAuditService;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealPricing;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentProvider paymentProvider;
    private final PaymentRepository paymentRepository;
    private final PaymentVerificationService paymentVerificationService;
    private final DealAuditService auditService;
    private final String providerName;

    public PaymentService(
        PaymentProvider paymentProvider,
        PaymentRepository paymentRepository,
        PaymentVerificationService paymentVerificationService,
        DealAuditService auditService
    ) {
        this.paymentProvider = paymentProvider;
        this.paymentRepository = paymentRepository;
        this.paymentVerificationService = paymentVerificationService;
        this.auditService = auditService;
        this.providerName = paymentProvider.getClass().getSimpleName();
    }

    public PaymentResult charge(Deal deal, AppUser payer) {
        String reference = paymentVerificationService.requireVerifiedReference(deal);
        PaymentResult result = paymentProvider.charge(deal, payer, reference);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.CHARGE,
            providerName,
            result.providerReference(),
            result.status(),
            DealPricing.totalToPay(deal)
        ));
        recordPaymentAudit(deal, payer.getId(), PaymentRecordType.CHARGE, result);
        return result;
    }

    public PaymentResult verifyCharge(Deal deal, String providerReference) {
        paymentVerificationService.validateAndConsumeReference(deal, providerReference);
        PaymentResult result = paymentProvider.verifyCharge(providerReference);
        paymentRepository.save(new PaymentRecord(
            deal.getId(),
            PaymentRecordType.CHARGE,
            providerName,
            result.providerReference(),
            result.status(),
            DealPricing.totalToPay(deal)
        ));
        recordPaymentAudit(deal, null, PaymentRecordType.CHARGE, result);
        return result;
    }

    public PaymentResult chargeDirect(AppUser payer, BigDecimal amount, String reference) {
        PaymentResult result = paymentProvider.chargeDirect(payer, amount, reference);
        paymentRepository.save(new PaymentRecord(
            null, PaymentRecordType.TOPUP, providerName, result.providerReference(), result.status(), amount
        ));
        return result;
    }

    public PaymentResult verifyDirectCharge(String providerReference, BigDecimal amount) {
        PaymentResult result = paymentProvider.verifyCharge(providerReference);
        paymentRepository.save(new PaymentRecord(
            null, PaymentRecordType.TOPUP, providerName, result.providerReference(), result.status(), amount
        ));
        return result;
    }

    public PaymentRecord findLatestByReference(String providerReference, PaymentRecordType type) {
        return paymentRepository.findFirstByProviderReferenceAndTypeOrderByCreatedAtDesc(providerReference, type)
            .orElseThrow(() -> new IllegalArgumentException("No payment record found for reference " + providerReference));
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
        recordPaymentAudit(deal, null, PaymentRecordType.PAYOUT, result);
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
        recordPaymentAudit(deal, null, PaymentRecordType.REFUND, result);
        return result;
    }

    /**
     * Looks up the id of the most recent successful record of the given type for a deal, so
     * callers (e.g. the wallet ledger) can attribute a ledger entry to the payment record that
     * triggered it without this service having to change its existing return types.
     */
    public Long latestSuccessfulRecordId(Long dealId, PaymentRecordType type) {
        return paymentRepository.findFirstByDealIdAndTypeAndStatusOrderByCreatedAtDesc(dealId, type, PaymentStatus.SUCCEEDED)
            .map(PaymentRecord::getId)
            .orElse(null);
    }

    private void recordPaymentAudit(Deal deal, Long actorUserId, PaymentRecordType type, PaymentResult result) {
        auditService.record(
            deal.getId(),
            DealAuditEventType.PAYMENT_ACTION,
            actorUserId,
            type + " via " + providerName + ": " + result.status(),
            Map.of(
                "type", type.name(),
                "provider", providerName,
                "providerReference", result.providerReference() == null ? "" : result.providerReference(),
                "status", result.status().name(),
                "message", result.message() == null ? "" : result.message()
            )
        );
    }
}
