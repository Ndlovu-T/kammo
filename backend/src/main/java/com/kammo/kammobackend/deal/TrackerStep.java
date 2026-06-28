package com.kammo.kammobackend.deal;

/**
 * The public-facing 5-step payment tracker advertised on kammo.co.za, plus the
 * exception categories that fall outside the happy path.
 */
public enum TrackerStep {
    INITIATED,
    PAID,
    TRANSIT,
    INSPECT,
    RELEASED,
    DISPUTED,
    ENDED
}
