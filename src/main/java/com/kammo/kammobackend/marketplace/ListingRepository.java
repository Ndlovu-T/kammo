package com.kammo.kammobackend.marketplace;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    List<Listing> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
