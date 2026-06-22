package com.kammo.kammobackend.rating;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRatingRepository extends JpaRepository<DealRating, Long> {

    List<DealRating> findByRaterUserId(Long raterUserId);
}
