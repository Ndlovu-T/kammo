package com.kammo.kammobackend.deal;

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
@Table(name = "deals")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String dealCode;

    @Column(nullable = false)
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DealRole ownerRole;

    @Column(nullable = false, length = 160)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    private String otherPartyPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod deliveryMethod;

    @Column(nullable = false)
    private Integer inspectionWindowHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DealStatus status = DealStatus.CREATED;

    @Column(length = 40)
    private String waybillNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    protected Deal() {
    }

    public Deal(
        String dealCode,
        Long ownerUserId,
        DealRole ownerRole,
        String itemName,
        BigDecimal price,
        String description,
        String otherPartyPhoneNumber,
        DeliveryMethod deliveryMethod,
        Integer inspectionWindowHours
    ) {
        this.dealCode = dealCode;
        this.ownerUserId = ownerUserId;
        this.ownerRole = ownerRole;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
        this.otherPartyPhoneNumber = otherPartyPhoneNumber;
        this.deliveryMethod = deliveryMethod;
        this.inspectionWindowHours = inspectionWindowHours;
    }

    public Long getId() {
        return id;
    }

    public String getDealCode() {
        return dealCode;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public DealRole getOwnerRole() {
        return ownerRole;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getOtherPartyPhoneNumber() {
        return otherPartyPhoneNumber;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public Integer getInspectionWindowHours() {
        return inspectionWindowHours;
    }

    public DealStatus getStatus() {
        return status;
    }

    public String getWaybillNumber() {
        return waybillNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(DealStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setWaybillNumber(String waybillNumber) {
        this.waybillNumber = waybillNumber;
        this.updatedAt = Instant.now();
    }
}
