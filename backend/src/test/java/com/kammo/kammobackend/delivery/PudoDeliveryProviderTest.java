package com.kammo.kammobackend.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.kammo.kammobackend.deal.Address;
import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DeliveryMethod;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PudoDeliveryProviderTest {

    private final PudoDeliveryProvider provider = new PudoDeliveryProvider();

    private Address lockerAddress;
    private Address doorAddress;

    @BeforeEach
    void setUp() {
        lockerAddress = new Address(null, null, null, null, null, null, null, "Lerato", "0710000001", null, "CPT001");
        doorAddress = new Address(
            "12 Long St", null, "Cape Town", "Western Cape", "8001", null, null, "Lerato", "0710000001", null, null
        );
    }

    private Deal dealWith(BigDecimal price, Address collectionAddress, Address deliveryAddress, DeliveryMethod deliveryMethod) {
        Deal deal = new Deal(
            "DEAL0001",
            1L,
            DealRole.BUYER,
            "Widget",
            price,
            "A widget",
            "0710000002",
            deliveryMethod,
            24,
            collectionAddress,
            deliveryAddress
        );
        ReflectionTestUtils.setField(deal, "id", 10L);
        return deal;
    }

    @Test
    void getRate_chargesLockerToLockerFeeWhenBothEndsAreLockers() {
        Deal deal = dealWith(new BigDecimal("100.00"), lockerAddress, lockerAddress, DeliveryMethod.PUDO_LOCKER);

        DeliveryRateQuote quote = provider.getRate(deal);

        assertThat(quote.fee()).isEqualByComparingTo("59.99");
        assertThat(quote.serviceName()).isEqualTo("PUDO Locker-to-Locker");
    }

    @Test
    void getRate_chargesDoorToDoorFeeWhenNeitherEndIsLocker() {
        Deal deal = dealWith(new BigDecimal("100.00"), doorAddress, doorAddress, DeliveryMethod.COURIER);

        DeliveryRateQuote quote = provider.getRate(deal);

        assertThat(quote.fee()).isEqualByComparingTo("119.99");
        assertThat(quote.serviceName()).isEqualTo("PUDO Door-to-Door");
    }

    @Test
    void getRate_addsInsuranceSurchargeForHighValueDeals() {
        Deal deal = dealWith(new BigDecimal("10000.00"), lockerAddress, lockerAddress, DeliveryMethod.PUDO_LOCKER);

        DeliveryRateQuote quote = provider.getRate(deal);

        assertThat(quote.fee()).isEqualByComparingTo("109.99");
    }

    @Test
    void createShipment_failsForMeetupDeliveries() {
        Deal deal = dealWith(new BigDecimal("100.00"), null, null, DeliveryMethod.MEETUP);

        ShipmentResult result = provider.createShipment(deal);

        assertThat(result.status()).isEqualTo(ShipmentStatus.FAILED);
        assertThat(result.waybillNumber()).isNull();
    }

    @Test
    void createShipment_returnsTrackableWaybillForCourierDeliveries() {
        Deal deal = dealWith(new BigDecimal("100.00"), doorAddress, doorAddress, DeliveryMethod.COURIER);

        ShipmentResult result = provider.createShipment(deal);

        assertThat(result.status()).isEqualTo(ShipmentStatus.SUCCEEDED);
        assertThat(result.waybillNumber()).startsWith("PD" + deal.getDealCode());
        assertThat(result.trackingUrl()).contains(result.waybillNumber());
    }

    @Test
    void trackShipment_returnsUnknownForUnrecognizedWaybill() {
        TrackingResult result = provider.trackShipment("does-not-exist");

        assertThat(result.status()).isEqualTo(TrackingStatus.UNKNOWN);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void trackShipment_startsAtAwaitingCollectionRightAfterCreation() {
        Deal deal = dealWith(new BigDecimal("100.00"), doorAddress, doorAddress, DeliveryMethod.COURIER);
        ShipmentResult shipment = provider.createShipment(deal);

        TrackingResult result = provider.trackShipment(shipment.waybillNumber());

        assertThat(result.status()).isEqualTo(TrackingStatus.AWAITING_COLLECTION);
        assertThat(result.events()).hasSize(1);
    }

    @Test
    void getLockers_returnsAllLockersWhenNoSearchTermGiven() {
        assertThat(provider.getLockers(null)).isNotEmpty();
        assertThat(provider.getLockers("")).isNotEmpty();
    }

    @Test
    void getLockers_filtersByCity() {
        assertThat(provider.getLockers("cape town"))
            .isNotEmpty()
            .allMatch(locker -> locker.city().equalsIgnoreCase("Cape Town"));
    }

    @Test
    void getLockers_filtersByTerminalId() {
        assertThat(provider.getLockers("JHB002"))
            .extracting(Locker::terminalId)
            .containsExactly("JHB002");
    }
}
