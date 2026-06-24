package com.kammo.kammobackend.deal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(length = 160)
    private String line1;

    @Column(length = 160)
    private String line2;

    @Column(length = 80)
    private String city;

    @Column(length = 80)
    private String province;

    @Column(length = 12)
    private String postalCode;

    private Double latitude;

    private Double longitude;

    @Column(length = 100)
    private String contactName;

    @Column(length = 30)
    private String contactPhone;

    @Column(length = 160)
    private String contactEmail;

    @Column(length = 40)
    private String lockerTerminalId;

    protected Address() {
    }

    public Address(
        String line1,
        String line2,
        String city,
        String province,
        String postalCode,
        Double latitude,
        Double longitude,
        String contactName,
        String contactPhone,
        String contactEmail,
        String lockerTerminalId
    ) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.lockerTerminalId = lockerTerminalId;
    }

    public static Address from(DealAddressRequest request) {
        if (request == null) {
            return null;
        }
        return new Address(
            request.line1(),
            request.line2(),
            request.city(),
            request.province(),
            request.postalCode(),
            request.latitude(),
            request.longitude(),
            request.contactName(),
            request.contactPhone(),
            request.contactEmail(),
            request.lockerTerminalId()
        );
    }

    public boolean isLockerPickup() {
        return lockerTerminalId != null && !lockerTerminalId.isBlank();
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getLockerTerminalId() {
        return lockerTerminalId;
    }
}
