package com.kammo.kammobackend.marketplace;

import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;

public record ListingResponse(
    Long id,
    String category,
    String name,
    BigDecimal price,
    String location,
    String sellerUsername,
    String description,
    boolean kammoSecured
) {
    public static ListingResponse from(Listing listing, AppUser seller) {
        return new ListingResponse(
            listing.getId(),
            listing.getCategory(),
            listing.getItemName(),
            listing.getPrice(),
            listing.getLocation(),
            "@" + seller.getEmail().split("@")[0].replaceAll("[^A-Za-z0-9_]", "_"),
            listing.getDescription(),
            true
        );
    }
}
