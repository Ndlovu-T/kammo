package com.kammo.kammobackend.deal;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "collection_line1", length = 160)),
        @AttributeOverride(name = "line2", column = @Column(name = "collection_line2", length = 160)),
        @AttributeOverride(name = "city", column = @Column(name = "collection_city", length = 80)),
        @AttributeOverride(name = "province", column = @Column(name = "collection_province", length = 80)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "collection_postal_code", length = 12)),
        @AttributeOverride(name = "latitude", column = @Column(name = "collection_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "collection_longitude")),
        @AttributeOverride(name = "contactName", column = @Column(name = "collection_contact_name", length = 100)),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "collection_contact_phone", length = 30)),
        @AttributeOverride(name = "contactEmail", column = @Column(name = "collection_contact_email", length = 160)),
        @AttributeOverride(name = "lockerTerminalId", column = @Column(name = "collection_locker_terminal_id", length = 40))
    })
    private Address collectionAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "delivery_line1", length = 160)),
        @AttributeOverride(name = "line2", column = @Column(name = "delivery_line2", length = 160)),
        @AttributeOverride(name = "city", column = @Column(name = "delivery_city", length = 80)),
        @AttributeOverride(name = "province", column = @Column(name = "delivery_province", length = 80)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "delivery_postal_code", length = 12)),
        @AttributeOverride(name = "latitude", column = @Column(name = "delivery_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "delivery_longitude")),
        @AttributeOverride(name = "contactName", column = @Column(name = "delivery_contact_name", length = 100)),
        @AttributeOverride(name = "contactPhone", column = @Column(name = "delivery_contact_phone", length = 30)),
        @AttributeOverride(name = "contactEmail", column = @Column(name = "delivery_contact_email", length = 160)),
        @AttributeOverride(name = "lockerTerminalId", column = @Column(name = "delivery_locker_terminal_id", length = 40))
    })
    private Address deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DealStatus status = DealStatus.CREATED;

    @Column(length = 40)
    private String waybillNumber;

    private Long listingId;

    private Instant deliveredAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

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
        Integer inspectionWindowHours,
        Address collectionAddress,
        Address deliveryAddress
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
        this.collectionAddress = collectionAddress;
        this.deliveryAddress = deliveryAddress;
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

    public Address getCollectionAddress() {
        return collectionAddress;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public DealStatus getStatus() {
        return status;
    }

    public String getWaybillNumber() {
        return waybillNumber;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
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
