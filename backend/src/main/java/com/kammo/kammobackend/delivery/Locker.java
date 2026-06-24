package com.kammo.kammobackend.delivery;

public record Locker(
    String terminalId,
    String name,
    String address,
    String city,
    String province,
    Double latitude,
    Double longitude
) {
}
