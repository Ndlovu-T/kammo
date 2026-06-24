package com.kammo.kammobackend.deal;

import com.kammo.kammobackend.delivery.DeliveryProvider;
import com.kammo.kammobackend.delivery.ShipmentResult;
import com.kammo.kammobackend.delivery.ShipmentStatus;
import com.kammo.kammobackend.delivery.TrackingResult;
import com.kammo.kammobackend.message.CreateMessageRequest;
import com.kammo.kammobackend.message.DealMessage;
import com.kammo.kammobackend.message.DealMessageRepository;
import com.kammo.kammobackend.payment.PaymentResult;
import com.kammo.kammobackend.payment.PaymentService;
import com.kammo.kammobackend.payment.PaymentStatus;
import com.kammo.kammobackend.rating.CreateRatingRequest;
import com.kammo.kammobackend.rating.DealRating;
import com.kammo.kammobackend.rating.DealRatingRepository;
import com.kammo.kammobackend.user.AppUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealService {

    private static final List<DealStatus> CANCELLABLE_STATUSES = List.of(
        DealStatus.CREATED,
        DealStatus.AWAITING_BUYER_PAYMENT,
        DealStatus.BUYER_ACCEPTED,
        DealStatus.SELLER_ACCEPTED
    );

    private final DealRepository dealRepository;
    private final DealCodeGenerator dealCodeGenerator;
    private final DealMessageRepository messageRepository;
    private final DealRatingRepository ratingRepository;
    private final PaymentService paymentService;
    private final DeliveryProvider deliveryProvider;
    private final ApplicationEventPublisher eventPublisher;

    public DealService(
        DealRepository dealRepository,
        DealCodeGenerator dealCodeGenerator,
        DealMessageRepository messageRepository,
        DealRatingRepository ratingRepository,
        PaymentService paymentService,
        DeliveryProvider deliveryProvider,
        ApplicationEventPublisher eventPublisher
    ) {
        this.dealRepository = dealRepository;
        this.dealCodeGenerator = dealCodeGenerator;
        this.messageRepository = messageRepository;
        this.ratingRepository = ratingRepository;
        this.paymentService = paymentService;
        this.deliveryProvider = deliveryProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DealResponse createDeal(AppUser user, CreateDealRequest request) {
        return DealResponse.from(buildAndSaveDeal(user, request, null));
    }

    @Transactional
    public DealResponse createDealForListing(AppUser user, CreateDealRequest request, Long listingId) {
        return DealResponse.from(buildAndSaveDeal(user, request, listingId));
    }

    private Deal buildAndSaveDeal(AppUser user, CreateDealRequest request, Long listingId) {
        validateAddresses(request.deliveryMethod(), request.collectionAddress(), request.deliveryAddress());
        Deal deal = new Deal(
            dealCodeGenerator.generateUniqueDealCode(),
            user.getId(),
            request.ownerRole(),
            request.itemName(),
            request.price(),
            request.description(),
            request.otherPartyPhoneNumber(),
            request.deliveryMethod(),
            request.inspectionWindowHours(),
            Address.from(request.collectionAddress()),
            Address.from(request.deliveryAddress())
        );
        deal.setListingId(listingId);
        if (request.ownerRole() == DealRole.SELLER) {
            deal.setStatus(DealStatus.AWAITING_BUYER_PAYMENT);
        }

        return dealRepository.save(deal);
    }

    @Transactional
    public DealResponse createSellerDeal(AppUser user, SellerDealRequest request) {
        validateAddresses(request.deliveryMethod(), request.collectionAddress(), request.deliveryAddress());
        Deal deal = new Deal(
            dealCodeGenerator.generateUniqueDealCode(),
            user.getId(),
            DealRole.SELLER,
            request.itemName(),
            request.price(),
            request.description(),
            request.buyerPhoneNumber(),
            request.deliveryMethod(),
            request.inspectionWindowHours(),
            Address.from(request.collectionAddress()),
            Address.from(request.deliveryAddress())
        );
        deal.setStatus(DealStatus.AWAITING_BUYER_PAYMENT);

        return DealResponse.from(dealRepository.save(deal));
    }

    private void validateAddresses(DeliveryMethod deliveryMethod, DealAddressRequest collectionAddress, DealAddressRequest deliveryAddress) {
        if (deliveryMethod == DeliveryMethod.MEETUP) {
            return;
        }
        requireUsableAddress(collectionAddress, "collection");
        requireUsableAddress(deliveryAddress, "delivery");
    }

    private void requireUsableAddress(DealAddressRequest address, String label) {
        if (address == null) {
            throw new IllegalArgumentException(
                "A " + label + " address or PUDO locker is required for courier and locker deliveries"
            );
        }
        if (address.lockerTerminalId() != null && !address.lockerTerminalId().isBlank()) {
            return;
        }
        boolean missingStreetDetails = isBlank(address.line1())
            || isBlank(address.city())
            || isBlank(address.province())
            || isBlank(address.postalCode())
            || isBlank(address.contactName())
            || isBlank(address.contactPhone());
        if (missingStreetDetails) {
            throw new IllegalArgumentException(
                "The " + label + " address needs a street address, city, province, postal code and contact details, or a PUDO locker"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void assignWaybillIfNeeded(Deal deal) {
        if (deal.getDeliveryMethod() == DeliveryMethod.MEETUP || deal.getWaybillNumber() != null) {
            return;
        }
        ShipmentResult shipment = deliveryProvider.createShipment(deal);
        if (shipment.status() == ShipmentStatus.FAILED) {
            throw new IllegalArgumentException("Failed to create delivery shipment: " + shipment.message());
        }
        deal.setWaybillNumber(shipment.waybillNumber());
    }

    public List<DealResponse> getMyDeals(AppUser user, String role) {
        return dealRepository.findByOwnerUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .filter(deal -> role == null || role.isBlank() || role.equalsIgnoreCase("all")
                || deal.getOwnerRole().name().equalsIgnoreCase(role))
            .map(DealResponse::from)
            .toList();
    }

    public List<DealResponse> getSellerDeals(AppUser user) {
        List<Deal> ownedSellerDeals = dealRepository.findByOwnerUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .filter(deal -> deal.getOwnerRole() == DealRole.SELLER)
            .toList();
        List<Deal> invitedSellerDeals = dealRepository.findByOtherPartyPhoneNumberOrderByCreatedAtDesc(user.getPhoneNumber()).stream()
            .filter(deal -> deal.getOwnerRole() == DealRole.BUYER)
            .toList();

        return Stream.concat(ownedSellerDeals.stream(), invitedSellerDeals.stream())
            .distinct()
            .sorted(Comparator.comparing(Deal::getCreatedAt).reversed())
            .map(DealResponse::from)
            .toList();
    }

    public DealResponse getDeal(String dealCode) {
        return DealResponse.from(findDeal(dealCode));
    }

    @Transactional
    public DealResponse markPaymentSecured(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        requireAwaitingPayment(deal);

        PaymentResult result = paymentService.charge(deal, user);
        if (result.status() == PaymentStatus.FAILED) {
            throw new IllegalArgumentException("Payment failed: " + result.message());
        }
        if (result.status() == PaymentStatus.PENDING) {
            return DealResponse.from(deal, result.checkoutUrl());
        }

        deal.setStatus(DealStatus.PAYMENT_SECURED);
        assignWaybillIfNeeded(deal);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse confirmPayment(AppUser user, String dealCode, String providerReference) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        requireAwaitingPayment(deal);

        PaymentResult result = paymentService.verifyCharge(deal, providerReference);
        if (result.status() != PaymentStatus.SUCCEEDED) {
            throw new IllegalArgumentException("Payment could not be confirmed: " + result.message());
        }

        deal.setStatus(DealStatus.PAYMENT_SECURED);
        assignWaybillIfNeeded(deal);
        return DealResponse.from(deal);
    }

    private void requireAwaitingPayment(Deal deal) {
        if (deal.getStatus() != DealStatus.AWAITING_BUYER_PAYMENT
            && deal.getStatus() != DealStatus.BUYER_ACCEPTED
            && deal.getStatus() != DealStatus.SELLER_ACCEPTED) {
            throw new IllegalArgumentException("This deal is not awaiting payment");
        }
    }

    @Transactional
    public DealResponse acceptAsSeller(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireSellerAccess(user, deal);
        if (deal.getStatus() != DealStatus.CREATED) {
            throw new IllegalArgumentException("Only newly created buyer deals can be accepted by the seller");
        }

        deal.setStatus(DealStatus.SELLER_ACCEPTED);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse acceptAsBuyer(AppUser user, String dealCode, BuyerAcceptDealRequest request) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        if (deal.getOwnerRole() != DealRole.SELLER) {
            throw new IllegalArgumentException("Only seller-created deals can be accepted by the buyer");
        }
        if (deal.getStatus() != DealStatus.AWAITING_BUYER_PAYMENT) {
            throw new IllegalArgumentException("This deal is not waiting for buyer acceptance");
        }

        String expectedReference = "KM-" + deal.getDealCode();
        if (!expectedReference.equalsIgnoreCase(request.dealReference())) {
            throw new IllegalArgumentException("Deal reference does not match the deal code");
        }

        deal.setStatus(DealStatus.BUYER_ACCEPTED);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse markReadyForCollection(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireSellerAccess(user, deal);
        if (deal.getStatus() != DealStatus.PAYMENT_SECURED) {
            throw new IllegalArgumentException("Payment must be secured before the seller can prepare collection");
        }

        deal.setStatus(DealStatus.AWAITING_COLLECTION);
        assignWaybillIfNeeded(deal);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse markInTransit(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireSellerAccess(user, deal);
        if (deal.getStatus() != DealStatus.AWAITING_COLLECTION) {
            throw new IllegalArgumentException("This deal is not awaiting collection");
        }

        deal.setStatus(DealStatus.IN_TRANSIT);
        assignWaybillIfNeeded(deal);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse markDelivered(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        if (deal.getStatus() != DealStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("This deal is not in transit");
        }

        deal.setStatus(DealStatus.DELIVERED);
        deal.setDeliveredAt(Instant.now());
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse confirmDelivery(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        if (deal.getStatus() != DealStatus.IN_TRANSIT
            && deal.getStatus() != DealStatus.AWAITING_COLLECTION
            && deal.getStatus() != DealStatus.DELIVERED) {
            throw new IllegalArgumentException("This deal is not in transit, delivered or awaiting collection");
        }

        deal.setStatus(DealStatus.COMPLETED);
        paymentService.payout(deal);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse cancelDeal(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        if (!CANCELLABLE_STATUSES.contains(deal.getStatus())) {
            throw new IllegalArgumentException("This deal can no longer be cancelled");
        }

        deal.setStatus(DealStatus.CANCELLED);
        if (deal.getListingId() != null) {
            eventPublisher.publishEvent(new DealCancelledEvent(deal.getListingId()));
        }
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse raiseDispute(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        if (deal.getStatus() == DealStatus.COMPLETED
            || deal.getStatus() == DealStatus.DISPUTED
            || deal.getStatus() == DealStatus.CANCELLED
            || deal.getStatus() == DealStatus.REFUNDED) {
            throw new IllegalArgumentException("This deal cannot be disputed in its current state");
        }
        if (deal.getDeliveredAt() != null) {
            Instant windowExpiry = deal.getDeliveredAt().plus(deal.getInspectionWindowHours(), ChronoUnit.HOURS);
            if (Instant.now().isAfter(windowExpiry)) {
                throw new IllegalArgumentException("The inspection window for this deal has expired");
            }
        }

        deal.setStatus(DealStatus.DISPUTED);
        return DealResponse.from(deal);
    }

    public List<DealResponse> getDisputedDeals() {
        return dealRepository.findByStatusOrderByCreatedAtDesc(DealStatus.DISPUTED).stream()
            .map(DealResponse::from)
            .toList();
    }

    @Transactional
    public DealResponse resolveDispute(String dealCode, DisputeResolution resolution) {
        Deal deal = findDeal(dealCode);
        if (deal.getStatus() != DealStatus.DISPUTED) {
            throw new IllegalArgumentException("This deal is not under dispute");
        }

        if (resolution == DisputeResolution.RELEASE_SELLER) {
            paymentService.payout(deal);
            deal.setStatus(DealStatus.COMPLETED);
        } else if (resolution == DisputeResolution.REFUND_BUYER) {
            paymentService.refund(deal);
            deal.setStatus(DealStatus.REFUNDED);
        }

        return DealResponse.from(deal);
    }

    public TrackingResult getTracking(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        if (deal.getWaybillNumber() == null) {
            throw new IllegalArgumentException("No shipment has been created for this deal yet");
        }
        return deliveryProvider.trackShipment(deal.getWaybillNumber());
    }

    public List<Map<String, Object>> getMessages(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        return messageRepository.findByDealIdOrderByCreatedAtAsc(deal.getId()).stream()
            .map(message -> Map.<String, Object>of(
                "id", message.getId(),
                "senderUserId", message.getSenderUserId(),
                "body", message.getBody(),
                "createdAt", message.getCreatedAt()
            ))
            .toList();
    }

    @Transactional
    public Map<String, Object> sendMessage(AppUser user, String dealCode, CreateMessageRequest request) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        DealMessage message = messageRepository.save(new DealMessage(deal.getId(), user.getId(), request.body()));
        return Map.of(
            "id", message.getId(),
            "senderUserId", message.getSenderUserId(),
            "body", message.getBody(),
            "createdAt", message.getCreatedAt()
        );
    }

    @Transactional
    public Map<String, Object> rateDeal(AppUser user, String dealCode, CreateRatingRequest request) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        if (deal.getStatus() != DealStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed deals can be rated");
        }
        if (ratingRepository.existsByDealIdAndRaterUserId(deal.getId(), user.getId())) {
            throw new IllegalArgumentException("You have already rated this deal");
        }

        DealRating rating = ratingRepository.save(
            new DealRating(deal.getId(), user.getId(), request.score(), request.comment())
        );
        return Map.of(
            "dealCode", deal.getDealCode(),
            "score", rating.getScore(),
            "comment", rating.getComment() == null ? "" : rating.getComment(),
            "createdAt", rating.getCreatedAt()
        );
    }

    public Deal findDeal(String dealCode) {
        return dealRepository.findByDealCode(dealCode)
            .orElseThrow(() -> new IllegalArgumentException("Deal not found"));
    }

    private void requireSellerAccess(AppUser user, Deal deal) {
        boolean sellerCreatedDeal = deal.getOwnerRole() == DealRole.SELLER && deal.getOwnerUserId().equals(user.getId());
        boolean buyerInvitedSeller = deal.getOwnerRole() == DealRole.BUYER
            && deal.getOtherPartyPhoneNumber().equals(user.getPhoneNumber());
        if (!sellerCreatedDeal && !buyerInvitedSeller) {
            throw new IllegalArgumentException("Only the seller can perform this action");
        }
    }

    private void requireBuyerAccess(AppUser user, Deal deal) {
        boolean sellerInvitedBuyer = deal.getOwnerRole() == DealRole.SELLER
            && deal.getOtherPartyPhoneNumber().equals(user.getPhoneNumber());
        boolean buyerCreatedDeal = deal.getOwnerRole() == DealRole.BUYER && deal.getOwnerUserId().equals(user.getId());
        if (!sellerInvitedBuyer && !buyerCreatedDeal) {
            throw new IllegalArgumentException("Only the buyer can perform this action");
        }
    }

    private void requireParticipant(AppUser user, Deal deal) {
        boolean isOwner = deal.getOwnerUserId().equals(user.getId());
        boolean isInvitedOtherParty = deal.getOtherPartyPhoneNumber().equals(user.getPhoneNumber());
        if (!isOwner && !isInvitedOtherParty) {
            throw new IllegalArgumentException("Only a participant in this deal can perform this action");
        }
    }
}
