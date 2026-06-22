package com.kammo.kammobackend.deal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {

    List<Deal> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    List<Deal> findByOtherPartyPhoneNumberOrderByCreatedAtDesc(String otherPartyPhoneNumber);

    Optional<Deal> findByDealCode(String dealCode);

    boolean existsByDealCode(String dealCode);
}
