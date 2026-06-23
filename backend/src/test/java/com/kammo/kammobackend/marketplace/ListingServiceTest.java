package com.kammo.kammobackend.marketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kammo.kammobackend.deal.CreateDealRequest;
import com.kammo.kammobackend.deal.DealResponse;
import com.kammo.kammobackend.deal.DealRole;
import com.kammo.kammobackend.deal.DealService;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.deal.DeliveryMethod;
import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DealService dealService;

    private ListingService listingService;

    private AppUser seller;
    private AppUser buyer;

    @BeforeEach
    void setUp() {
        listingService = new ListingService(listingRepository, userRepository, dealService);

        seller = new AppUser("KM0001", "0710000001", "seller@example.com", "hashed");
        ReflectionTestUtils.setField(seller, "id", 1L);

        buyer = new AppUser("KM0002", "0710000002", "buyer@example.com", "hashed");
        ReflectionTestUtils.setField(buyer, "id", 2L);
    }

    private Listing activeListing() {
        Listing listing = new Listing(seller.getId(), "Electronics", "Phone", new BigDecimal("500.00"), "Cape Town", "A phone");
        ReflectionTestUtils.setField(listing, "id", 100L);
        return listing;
    }

    @Test
    void updateListing_rejectsNonOwner() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        UpdateListingRequest request = new UpdateListingRequest("Electronics", "Phone", new BigDecimal("600.00"), "Cape Town", "Updated");

        assertThatThrownBy(() -> listingService.updateListing(buyer, 100L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateListing_succeedsForOwner() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        UpdateListingRequest request = new UpdateListingRequest("Electronics", "Phone X", new BigDecimal("600.00"), "Cape Town", "Updated");

        ListingResponse response = listingService.updateListing(seller, 100L, request);

        assertThat(response.name()).isEqualTo("Phone X");
        assertThat(listing.getPrice()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    void removeListing_rejectsNonOwner() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.removeListing(buyer, 100L))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    void removeListing_succeedsForOwner() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        listingService.removeListing(seller, 100L);

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.REMOVED);
    }

    @Test
    void buyListing_rejectsBuyingOwnListing() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.buyListing(seller, 100L))
            .isInstanceOf(IllegalArgumentException.class);
        verify(dealService, never()).createDeal(any(), any());
    }

    @Test
    void buyListing_rejectsNonActiveListing() {
        Listing listing = activeListing();
        listing.setStatus(ListingStatus.SOLD);
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.buyListing(buyer, 100L))
            .isInstanceOf(IllegalArgumentException.class);
        verify(dealService, never()).createDeal(any(), any());
    }

    @Test
    void buyListing_createsDealAndMarksListingSoldOnSuccess() {
        Listing listing = activeListing();
        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));
        DealResponse dealResponse = new DealResponse(
            "DEAL0001", "KM-DEAL0001", "https://kammo.co.za/deal/DEAL0001",
            DealRole.BUYER, listing.getItemName(), listing.getPrice(), BigDecimal.ZERO, BigDecimal.ZERO, listing.getPrice(),
            listing.getDescription(), seller.getPhoneNumber(), "next buyer action", "next seller action",
            DeliveryMethod.MEETUP, 24, DealStatus.CREATED, null, Instant.now(), Instant.now()
        );
        when(dealService.createDeal(eq(buyer), any(CreateDealRequest.class))).thenReturn(dealResponse);

        DealResponse response = listingService.buyListing(buyer, 100L);

        assertThat(response).isEqualTo(dealResponse);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.SOLD);
        verify(listingRepository).save(listing);
    }

    @Test
    void getActiveListings_noFilterReturnsAllActiveListings() {
        Listing listing = activeListing();
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)).thenReturn(List.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        List<ListingResponse> responses = listingService.getActiveListings(null, null);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getActiveListings_blankOrAllCategoryMeansNoFilter() {
        Listing listing = activeListing();
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)).thenReturn(List.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        assertThat(listingService.getActiveListings("", null)).hasSize(1);
        assertThat(listingService.getActiveListings("ALL", null)).hasSize(1);
    }

    @Test
    void getActiveListings_categoryFilterIsCaseInsensitive() {
        Listing listing = activeListing();
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)).thenReturn(List.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        assertThat(listingService.getActiveListings("electronics", null)).hasSize(1);
        assertThat(listingService.getActiveListings("furniture", null)).isEmpty();
    }

    @Test
    void getActiveListings_searchFilterMatchesItemNameCaseInsensitively() {
        Listing listing = activeListing();
        when(listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE)).thenReturn(List.of(listing));
        when(userRepository.findById(seller.getId())).thenReturn(Optional.of(seller));

        assertThat(listingService.getActiveListings(null, "phone")).hasSize(1);
        assertThat(listingService.getActiveListings(null, "PHONE")).hasSize(1);
        assertThat(listingService.getActiveListings(null, "laptop")).isEmpty();
    }
}
