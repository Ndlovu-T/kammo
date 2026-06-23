package com.kammo.kammobackend.message;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealMessageRepository extends JpaRepository<DealMessage, Long> {

    List<DealMessage> findByDealIdOrderByCreatedAtAsc(Long dealId);
}
