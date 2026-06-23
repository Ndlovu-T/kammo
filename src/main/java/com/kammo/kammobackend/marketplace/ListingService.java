package com.kammo.kammobackend.marketplace;

import com.kammo.kammobackend.deal.CreateDealRequest;
import com.kammo.kammobackend.deal.DealResponse;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealService;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final DealService dealService;

    public ListingService(
        ListingRepository listingRepository,
        UserRepository userRepository,
        DealService dealService
    ) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.dealService = dealService;
    }

    public List<ListingResponse> getActiveListings(String category, String search) {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE).stream()
            .filter(listing -> category == null || category.isBlank() || category.equalsIgnoreCase("all")
                || listing.getCategory().equalsIgnoreCase(category))
            .filter(listing -> search == null || search.isBlank()
                || listing.getItemName().toLowerCase().contains(search.toLowerCase()))
            .map(this::toResponse)
            .toList();
    }

    public ListingResponse getListing(Long id) {
        return toResponse(findListing(id));
    }

    public List<ListingResponse> getMyListings(AppUser user) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ListingResponse createListing(AppUser user, CreateListingRequest request) {
        Listing listing = new Listing(
            user.getId(),
            request.category(),
            request.itemName(),
            request.price(),
            request.location(),
            request.description()
        );
        return toResponse(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updateListing(AppUser user, Long id, UpdateListingRequest request) {
        Listing listing = findListing(id);
        requireOwner(user, listing, "Only the seller who created this listing can update it");

        listing.setCategory(request.category());
        listing.setItemName(request.itemName());
        listing.setPrice(request.price());
        listing.setLocation(request.location());
        listing.setDescription(request.description());

        return toResponse(listing);
    }

    @Transactional
    public void removeListing(AppUser user, Long id) {
        Listing listing = findListing(id);
        requireOwner(user, listing, "Only the seller who created this listing can remove it");
        listing.setStatus(ListingStatus.REMOVED);
    }

    @Transactional
    public DealResponse buyListing(AppUser user, Long id) {
        Listing listing = findListing(id);
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("This listing is no longer available");
        }
        if (listing.getSellerId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot buy your own listing");
        }

        AppUser seller = userRepository.findById(listing.getSellerId())
            .orElseThrow(() -> new IllegalArgumentException("Seller account not found"));

        CreateDealRequest request = new CreateDealRequest(
            DealRole.BUYER,
            listing.getItemName(),
            listing.getPrice(),
            listing.getDescription(),
            seller.getPhoneNumber(),
            DeliveryMethod.MEETUP,
            24
        );
        DealResponse dealResponse = dealService.createDeal(user, request);

        listing.setStatus(ListingStatus.SOLD);
        listingRepository.save(listing);

        return dealResponse;
    }

    private Listing findListing(Long id) {
        return listingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found"));
    }

    private void requireOwner(AppUser user, Listing listing, String message) {
        if (!listing.getSellerId().equals(user.getId())) {
            throw new IllegalArgumentException(message);
        }
    }

    private ListingResponse toResponse(Listing listing) {
        AppUser seller = userRepository.findById(listing.getSellerId())
            .orElseThrow(() -> new IllegalArgumentException("Seller account not found"));
        return ListingResponse.from(listing, seller);
    }
}
