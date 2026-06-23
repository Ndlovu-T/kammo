package com.kammo.kammobackend.marketplace;

import com.kammo.kammobackend.deal.DealResponse;
import com.kammo.kammobackend.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final ListingService listingService;

    public MarketplaceController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping("/listings")
    public List<ListingResponse> getListings(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search
    ) {
        return listingService.getActiveListings(category, search);
    }

    @GetMapping("/listings/mine")
    public List<ListingResponse> getMyListings(@AuthenticationPrincipal AppUser user) {
        return listingService.getMyListings(user);
    }

    @GetMapping("/listings/{id}")
    public ListingResponse getListing(@PathVariable Long id) {
        return listingService.getListing(id);
    }

    @PostMapping("/listings")
    public ResponseEntity<ListingResponse> createListing(
        @AuthenticationPrincipal AppUser user,
        @Valid @RequestBody CreateListingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.createListing(user, request));
    }

    @PutMapping("/listings/{id}")
    public ListingResponse updateListing(
        @AuthenticationPrincipal AppUser user,
        @PathVariable Long id,
        @Valid @RequestBody UpdateListingRequest request
    ) {
        return listingService.updateListing(user, id, request);
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> removeListing(
        @AuthenticationPrincipal AppUser user,
        @PathVariable Long id
    ) {
        listingService.removeListing(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/listings/{id}/buy")
    public ResponseEntity<DealResponse> buyListing(
        @AuthenticationPrincipal AppUser user,
        @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.buyListing(user, id));
    }
}
