package com.kammo.kammobackend.payment;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentVerificationRepository extends JpaRepository<PaymentVerification, Long> {

    Optional<PaymentVerification> findFirstByDealIdOrderByCreatedAtDesc(Long dealId);

    long countByDealIdAndCreatedAtAfter(Long dealId, Instant since);
}
