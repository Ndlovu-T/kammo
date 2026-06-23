package com.kammo.kammobackend.deal;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private final DealCodeGenerator dealCodeGenerator;
    private final DealMessageRepository messageRepository;
    private final DealRatingRepository ratingRepository;
    private final PaymentService paymentService;

    public DealService(
        DealRepository dealRepository,
        DealCodeGenerator dealCodeGenerator,
        DealMessageRepository messageRepository,
        DealRatingRepository ratingRepository,
        PaymentService paymentService
    ) {
        this.dealRepository = dealRepository;
        this.dealCodeGenerator = dealCodeGenerator;
        this.messageRepository = messageRepository;
        this.ratingRepository = ratingRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public DealResponse createDeal(AppUser user, CreateDealRequest request) {
        Deal deal = new Deal(
            dealCodeGenerator.generateUniqueDealCode(),
            user.getId(),
            request.ownerRole(),
            request.itemName(),
            request.price(),
            request.description(),
            request.otherPartyPhoneNumber(),
            request.deliveryMethod(),
            request.inspectionWindowHours()
        );
        if (request.ownerRole() == DealRole.SELLER) {
            deal.setStatus(DealStatus.AWAITING_BUYER_PAYMENT);
        }

        return DealResponse.from(dealRepository.save(deal));
    }

    @Transactional
    public DealResponse createSellerDeal(AppUser user, SellerDealRequest request) {
        Deal deal = new Deal(
            dealCodeGenerator.generateUniqueDealCode(),
            user.getId(),
            DealRole.SELLER,
            request.itemName(),
            request.price(),
            request.description(),
            request.buyerPhoneNumber(),
            request.deliveryMethod(),
            request.inspectionWindowHours()
        );
        deal.setStatus(DealStatus.AWAITING_BUYER_PAYMENT);

        return DealResponse.from(dealRepository.save(deal));
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
        if (deal.getStatus() != DealStatus.AWAITING_BUYER_PAYMENT
            && deal.getStatus() != DealStatus.BUYER_ACCEPTED
            && deal.getStatus() != DealStatus.SELLER_ACCEPTED) {
            throw new IllegalArgumentException("This deal is not awaiting payment");
        }

        PaymentResult result = paymentService.charge(deal, user);
        if (result.status() == PaymentStatus.FAILED) {
            throw new IllegalArgumentException("Payment failed: " + result.message());
        }

        deal.setStatus(DealStatus.PAYMENT_SECURED);
        if (deal.getDeliveryMethod() == DeliveryMethod.COURIER && deal.getWaybillNumber() == null) {
            deal.setWaybillNumber("TCG-" + deal.getDealCode() + "-001");
        }
        return DealResponse.from(deal);
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
        if (deal.getDeliveryMethod() == DeliveryMethod.COURIER && deal.getWaybillNumber() == null) {
            deal.setWaybillNumber("TCG-" + deal.getDealCode() + "-001");
        }
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
        if (deal.getWaybillNumber() == null) {
            deal.setWaybillNumber("TCG-" + deal.getDealCode() + "-001");
        }
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse confirmDelivery(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireBuyerAccess(user, deal);
        if (deal.getStatus() != DealStatus.IN_TRANSIT && deal.getStatus() != DealStatus.AWAITING_COLLECTION) {
            throw new IllegalArgumentException("This deal is not in transit or awaiting collection");
        }

        deal.setStatus(DealStatus.COMPLETED);
        paymentService.payout(deal);
        return DealResponse.from(deal);
    }

    @Transactional
    public DealResponse raiseDispute(AppUser user, String dealCode) {
        Deal deal = findDeal(dealCode);
        requireParticipant(user, deal);
        if (deal.getStatus() == DealStatus.COMPLETED || deal.getStatus() == DealStatus.DISPUTED) {
            throw new IllegalArgumentException("This deal cannot be disputed in its current state");
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
