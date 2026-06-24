package com.kammo.kammobackend.delivery;

import java.time.Instant;

public record TrackingEvent(TrackingStatus status, String description, Instant occurredAt) {
}
