package com.kammo.kammobackend.marketplace;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final List<ListingResponse> listings = List.of(
        new ListingResponse("iphone-14-pro-max", "Electronics", "iPhone 14 Pro Max", new BigDecimal("8500.00"), "Johannesburg", "@diski_tech", "4.9", "Excellent condition, battery 97%, original box and charger included.", true),
        new ListingResponse("macbook-air-m2", "Electronics", "MacBook Air M2", new BigDecimal("16500.00"), "Cape Town", "@mac_deals", "5.0", "Lightly used MacBook Air M2 with charger.", true),
        new ListingResponse("jordan-1-chicago", "Fashion", "Jordan 1 Chicago", new BigDecimal("4200.00"), "Johannesburg", "@kick_vault", "4.8", "Authentic Air Jordan 1 Chicago in excellent condition.", true),
        new ListingResponse("sony-a7-iv", "Electronics", "Sony A7 IV", new BigDecimal("22000.00"), "Durban", "@photo_pro", "4.9", "Sony A7 IV body with battery and charger.", true),
        new ListingResponse("ps5-bundle", "Gaming", "PS5 Bundle", new BigDecimal("12000.00"), "Pretoria", "@game_king", "4.7", "PS5 console bundle with two controllers and games.", true),
        new ListingResponse("apple-watch-ultra-2", "Electronics", "Apple Watch Ultra 2", new BigDecimal("9800.00"), "Cape Town", "@luxury_sa", "5.0", "Apple Watch Ultra 2 with original box.", true)
    );

    @GetMapping("/listings")
    public List<ListingResponse> getListings(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search
    ) {
        return listings.stream()
            .filter(listing -> category == null || category.isBlank() || category.equalsIgnoreCase("all")
                || listing.category().equalsIgnoreCase(category))
            .filter(listing -> search == null || search.isBlank()
                || listing.name().toLowerCase().contains(search.toLowerCase()))
            .toList();
    }

    @GetMapping("/listings/{id}")
    public ListingResponse getListing(@PathVariable String id) {
        return listings.stream()
            .filter(listing -> listing.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
    }

    public record ListingResponse(
        String id,
        String category,
        String name,
        BigDecimal price,
        String location,
        String sellerUsername,
        String sellerRating,
        String description,
        boolean kammoSecured
    ) {
    }
}
