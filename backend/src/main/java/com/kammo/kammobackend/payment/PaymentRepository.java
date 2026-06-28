package com.kammo.kammobackend.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findByDealIdOrderByCreatedAtDesc(Long dealId);

    Optional<PaymentRecord> findFirstByDealIdAndTypeAndStatusOrderByCreatedAtDesc(
        Long dealId, PaymentRecordType type, PaymentStatus status
    );

    Optional<PaymentRecord> findFirstByProviderReferenceAndTypeOrderByCreatedAtDesc(
        String providerReference, PaymentRecordType type
    );
}
