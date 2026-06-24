package com.kammo.kammobackend.delivery;

import com.kammo.kammobackend.deal.Deal;
import java.util.List;

public interface DeliveryProvider {

    DeliveryRateQuote getRate(Deal deal);

    ShipmentResult createShipment(Deal deal);

    TrackingResult trackShipment(String waybillNumber);

    List<Locker> getLockers(String searchTerm);
}
