package com.kammo.kammobackend.delivery;

import com.kammo.kammobackend.deal.Address;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DeliveryMethod;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PudoDeliveryProvider implements DeliveryProvider {

    private static final BigDecimal LOCKER_TO_LOCKER_FEE = new BigDecimal("59.99");
    private static final BigDecimal LOCKER_TO_DOOR_FEE = new BigDecimal("79.99");
    private static final BigDecimal DOOR_TO_DOOR_FEE = new BigDecimal("119.99");
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal HIGH_VALUE_INSURANCE_RATE = new BigDecimal("0.005");

    private static final List<Locker> LOCKERS = List.of(
        new Locker("CPT001", "Pick n Pay Sea Point", "2 Main Rd, Sea Point", "Cape Town", "Western Cape", -33.9152, 18.3849),
        new Locker("CPT014", "Checkers Tyger Valley", "Willie Van Schoor Dr, Bellville", "Cape Town", "Western Cape", -33.8696, 18.6321),
        new Locker("JHB002", "Pick n Pay Sandton City", "83 Rivonia Rd, Sandhurst", "Johannesburg", "Gauteng", -26.1076, 28.0567),
        new Locker("JHB031", "Checkers Hyde Park Corner", "Jan Smuts Ave, Hyde Park", "Johannesburg", "Gauteng", -26.1364, 28.0376),
        new Locker("PTA009", "Pick n Pay Brooklyn Mall", "Veale St, Brooklyn", "Pretoria", "Gauteng", -25.7615, 28.2373),
        new Locker("DBN004", "Pick n Pay Gateway", "1 Palm Blvd, Umhlanga", "Durban", "KwaZulu-Natal", -29.7237, 31.0686),
        new Locker("PE003", "Checkers Walmer Park", "Main Rd, Walmer", "Gqeberha", "Eastern Cape", -33.9885, 25.5747),
        new Locker("BFN002", "Pick n Pay Mimosa Mall", "Kellner St, Westdene", "Bloemfontein", "Free State", -29.1138, 26.1947)
    );

    private final Map<String, ShipmentRecord> shipments = new ConcurrentHashMap<>();

    @Override
    public DeliveryRateQuote getRate(Deal deal) {
        boolean collectionIsLocker = isLocker(deal.getCollectionAddress());
        boolean deliveryIsLocker = isLocker(deal.getDeliveryAddress());

        BigDecimal fee;
        int estimatedDays;
        String serviceName;
        if (collectionIsLocker && deliveryIsLocker) {
            fee = LOCKER_TO_LOCKER_FEE;
            estimatedDays = 2;
            serviceName = "PUDO Locker-to-Locker";
        } else if (collectionIsLocker || deliveryIsLocker) {
            fee = LOCKER_TO_DOOR_FEE;
            estimatedDays = 3;
            serviceName = "PUDO Locker-to-Door";
        } else {
            fee = DOOR_TO_DOOR_FEE;
            estimatedDays = 3;
            serviceName = "PUDO Door-to-Door";
        }

        if (deal.getPrice() != null && deal.getPrice().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            fee = fee.add(deal.getPrice().multiply(HIGH_VALUE_INSURANCE_RATE));
        }

        return new DeliveryRateQuote(serviceName, fee, estimatedDays);
    }

    @Override
    public ShipmentResult createShipment(Deal deal) {
        if (deal.getDeliveryMethod() == DeliveryMethod.MEETUP) {
            return new ShipmentResult(ShipmentStatus.FAILED, null, null, "PUDO does not handle meetup deliveries");
        }

        String waybillNumber = "PD" + deal.getDealCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        shipments.put(waybillNumber, new ShipmentRecord(deal.getDeliveryMethod(), Instant.now()));

        return new ShipmentResult(
            ShipmentStatus.SUCCEEDED,
            waybillNumber,
            "https://tracking.pudo.co.za/" + waybillNumber,
            "Shipment created"
        );
    }

    @Override
    public TrackingResult trackShipment(String waybillNumber) {
        ShipmentRecord record = shipments.get(waybillNumber);
        if (record == null) {
            return new TrackingResult(waybillNumber, TrackingStatus.UNKNOWN, List.of());
        }

        long minutesSinceCreation = Duration.between(record.createdAt(), Instant.now()).toMinutes();
        TrackingStatus finalStatus = record.deliveryMethod() == DeliveryMethod.PUDO_LOCKER
            ? TrackingStatus.AT_LOCKER
            : TrackingStatus.DELIVERED;

        List<TrackingEvent> events = new java.util.ArrayList<>();
        events.add(new TrackingEvent(
            TrackingStatus.AWAITING_COLLECTION, "Waybill generated, awaiting collection", record.createdAt()
        ));
        if (minutesSinceCreation >= 2) {
            events.add(new TrackingEvent(
                TrackingStatus.COLLECTED, "Parcel collected by courier", record.createdAt().plus(2, ChronoUnit.MINUTES)
            ));
        }
        if (minutesSinceCreation >= 5) {
            events.add(new TrackingEvent(
                TrackingStatus.IN_TRANSIT, "Parcel in transit", record.createdAt().plus(5, ChronoUnit.MINUTES)
            ));
        }
        if (minutesSinceCreation >= 10) {
            String description = finalStatus == TrackingStatus.AT_LOCKER
                ? "Parcel ready for collection at locker"
                : "Parcel delivered";
            events.add(new TrackingEvent(finalStatus, description, record.createdAt().plus(10, ChronoUnit.MINUTES)));
        }

        TrackingStatus currentStatus = events.get(events.size() - 1).status();
        return new TrackingResult(waybillNumber, currentStatus, List.copyOf(events));
    }

    @Override
    public List<Locker> getLockers(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return LOCKERS;
        }
        String normalized = searchTerm.trim().toLowerCase();
        return LOCKERS.stream()
            .filter(locker -> locker.city().toLowerCase().contains(normalized)
                || locker.province().toLowerCase().contains(normalized)
                || locker.name().toLowerCase().contains(normalized)
                || locker.terminalId().toLowerCase().contains(normalized))
            .toList();
    }

    private boolean isLocker(Address address) {
        return address != null && address.isLockerPickup();
    }

    private record ShipmentRecord(DeliveryMethod deliveryMethod, Instant createdAt) {
    }
}
