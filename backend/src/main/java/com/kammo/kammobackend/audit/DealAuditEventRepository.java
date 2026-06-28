package com.kammo.kammobackend.audit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealAuditEventRepository extends JpaRepository<DealAuditEvent, Long> {

    List<DealAuditEvent> findByDealIdOrderByCreatedAtAsc(Long dealId);
}
