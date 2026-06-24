package com.kammo.kammobackend.deal;

public record DealAddressResponse(
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
    public static DealAddressResponse from(Address address) {
        if (address == null) {
            return null;
        }
        return new DealAddressResponse(
            address.getLine1(),
            address.getLine2(),
            address.getCity(),
            address.getProvince(),
            address.getPostalCode(),
            address.getLatitude(),
            address.getLongitude(),
            address.getContactName(),
            address.getContactPhone(),
            address.getContactEmail(),
            address.getLockerTerminalId()
        );
    }
}
