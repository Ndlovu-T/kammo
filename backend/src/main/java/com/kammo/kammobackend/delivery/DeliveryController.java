package com.kammo.kammobackend.delivery;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryProvider deliveryProvider;

    public DeliveryController(DeliveryProvider deliveryProvider) {
        this.deliveryProvider = deliveryProvider;
    }

    @GetMapping("/lockers")
    public List<Locker> getLockers(@RequestParam(required = false) String search) {
        return deliveryProvider.getLockers(search);
    }
}
