package com.kammo.kammobackend.marketplace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 160)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 120)
    private String location;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    protected Listing() {
    }

    public Listing(
        Long sellerId,
        String category,
        String itemName,
        BigDecimal price,
        String location,
        String description
    ) {
        this.sellerId = sellerId;
        this.category = category;
        this.itemName = itemName;
        this.price = price;
        this.location = location;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getCategory() {
        return category;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = Instant.now();
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
        this.updatedAt = Instant.now();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.updatedAt = Instant.now();
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }
}
