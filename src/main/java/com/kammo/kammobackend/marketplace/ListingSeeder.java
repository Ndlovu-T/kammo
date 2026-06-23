package com.kammo.kammobackend.marketplace;

import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ListingSeeder implements ApplicationRunner {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingSeeder(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (listingRepository.count() != 0) {
            return;
        }

        List<AppUser> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }

        Long sellerId = users.get(0).getId();
        listingRepository.saveAll(List.of(
            new Listing(sellerId, "Electronics", "iPhone 14 Pro Max", new BigDecimal("8500.00"), "Johannesburg", "Excellent condition, battery 97%, original box and charger included."),
            new Listing(sellerId, "Electronics", "MacBook Air M2", new BigDecimal("16500.00"), "Cape Town", "Lightly used MacBook Air M2 with charger."),
            new Listing(sellerId, "Fashion", "Jordan 1 Chicago", new BigDecimal("4200.00"), "Johannesburg", "Authentic Air Jordan 1 Chicago in excellent condition."),
            new Listing(sellerId, "Electronics", "Sony A7 IV", new BigDecimal("22000.00"), "Durban", "Sony A7 IV body with battery and charger."),
            new Listing(sellerId, "Gaming", "PS5 Bundle", new BigDecimal("12000.00"), "Pretoria", "PS5 console bundle with two controllers and games."),
            new Listing(sellerId, "Electronics", "Apple Watch Ultra 2", new BigDecimal("9800.00"), "Cape Town", "Apple Watch Ultra 2 with original box.")
        ));
    }
}
