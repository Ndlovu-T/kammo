package com.kammo.kammobackend.deal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DealStatusTrackerStepTest {

    private static final Map<DealStatus, TrackerStep> EXPECTED = new EnumMap<>(DealStatus.class);

    static {
        EXPECTED.put(DealStatus.CREATED, TrackerStep.INITIATED);
        EXPECTED.put(DealStatus.AWAITING_BUYER_PAYMENT, TrackerStep.INITIATED);
        EXPECTED.put(DealStatus.BUYER_ACCEPTED, TrackerStep.INITIATED);
        EXPECTED.put(DealStatus.SELLER_ACCEPTED, TrackerStep.INITIATED);
        EXPECTED.put(DealStatus.PAYMENT_SECURED, TrackerStep.PAID);
        EXPECTED.put(DealStatus.AWAITING_COLLECTION, TrackerStep.TRANSIT);
        EXPECTED.put(DealStatus.IN_TRANSIT, TrackerStep.TRANSIT);
        EXPECTED.put(DealStatus.DELIVERED, TrackerStep.INSPECT);
        EXPECTED.put(DealStatus.COMPLETED, TrackerStep.RELEASED);
        EXPECTED.put(DealStatus.DISPUTED, TrackerStep.DISPUTED);
        EXPECTED.put(DealStatus.REFUNDED, TrackerStep.ENDED);
        EXPECTED.put(DealStatus.CANCELLED, TrackerStep.ENDED);
    }

    @Test
    void everyDealStatusMapsToExpectedTrackerStep() {
        for (DealStatus status : DealStatus.values()) {
            assertThat(status.toTrackerStep())
                .as("tracker step for %s", status)
                .isEqualTo(EXPECTED.get(status));
        }
    }

    @Test
    void everyDealStatusHasANonNullTrackerStep() {
        for (DealStatus status : DealStatus.values()) {
            assertThat(status.toTrackerStep()).as("tracker step for %s", status).isNotNull();
        }
    }

    @Test
    void testCoversEveryDealStatus() {
        assertThat(EXPECTED.keySet()).containsExactlyInAnyOrder(DealStatus.values());
    }
}
