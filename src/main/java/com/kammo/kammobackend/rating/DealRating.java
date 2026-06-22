package com.kammo.kammobackend.rating;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "deal_ratings")
public class DealRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dealId;

    @Column(nullable = false)
    private Long raterUserId;

    @Column(nullable = false)
    private Integer score;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DealRating() {
    }

    public DealRating(Long dealId, Long raterUserId, Integer score, String comment) {
        this.dealId = dealId;
        this.raterUserId = raterUserId;
        this.score = score;
        this.comment = comment;
    }

    public Long getDealId() {
        return dealId;
    }

    public Long getRaterUserId() {
        return raterUserId;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
