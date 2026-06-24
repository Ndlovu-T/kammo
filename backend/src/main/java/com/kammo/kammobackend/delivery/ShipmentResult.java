package com.kammo.kammobackend.delivery;

public record ShipmentResult(ShipmentStatus status, String waybillNumber, String trackingUrl, String message) {
}
