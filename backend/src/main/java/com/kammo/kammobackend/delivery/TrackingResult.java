package com.kammo.kammobackend.delivery;

import java.util.List;

public record TrackingResult(String waybillNumber, TrackingStatus status, List<TrackingEvent> events) {
}
