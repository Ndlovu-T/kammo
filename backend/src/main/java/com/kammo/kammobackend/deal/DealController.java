package com.kammo.kammobackend.deal;

import com.kammo.kammobackend.delivery.TrackingResult;
import com.kammo.kammobackend.message.CreateMessageRequest;
import com.kammo.kammobackend.rating.CreateRatingRequest;
import com.kammo.kammobackend.user.AppUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public List<DealResponse> getMyDeals(
        @AuthenticationPrincipal AppUser user,
        @RequestParam(required = false) String role
    ) {
        return dealService.getMyDeals(user, role);
    }

    @GetMapping("/seller")
    public List<DealResponse> getSellerDeals(@AuthenticationPrincipal AppUser user) {
        return dealService.getSellerDeals(user);
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(
        @AuthenticationPrincipal AppUser user,
        @Valid @RequestBody CreateDealRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.createDeal(user, request));
    }

    @PostMapping("/seller")
    public ResponseEntity<DealResponse> createSellerDeal(
        @AuthenticationPrincipal AppUser user,
        @Valid @RequestBody SellerDealRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.createSellerDeal(user, request));
    }

    @GetMapping("/{dealCode}")
    public DealResponse getDeal(@PathVariable String dealCode) {
        return dealService.getDeal(dealCode);
    }

    @PostMapping("/{dealCode}/payment/request-otp")
    public Map<String, Object> requestPaymentOtp(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.requestPaymentOtp(user, dealCode);
    }

    @PostMapping("/{dealCode}/payment/verify-otp")
    public Map<String, Object> verifyPaymentOtp(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode,
        @Valid @RequestBody VerifyPaymentOtpRequest request
    ) {
        return dealService.verifyPaymentOtp(user, dealCode, request.code());
    }

    @PostMapping("/{dealCode}/payment")
    public DealResponse markPaymentSecured(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.markPaymentSecured(user, dealCode);
    }

    @PostMapping("/{dealCode}/payment/confirm")
    public DealResponse confirmPayment(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode,
        @Valid @RequestBody ConfirmPaymentRequest request
    ) {
        return dealService.confirmPayment(user, dealCode, request.reference());
    }

    @PostMapping("/{dealCode}/seller/accept")
    public DealResponse acceptAsSeller(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.acceptAsSeller(user, dealCode);
    }

    @PostMapping("/{dealCode}/buyer/accept")
    public DealResponse acceptAsBuyer(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode,
        @Valid @RequestBody BuyerAcceptDealRequest request
    ) {
        return dealService.acceptAsBuyer(user, dealCode, request);
    }

    @PostMapping("/{dealCode}/seller/ready-for-collection")
    public DealResponse markReadyForCollection(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.markReadyForCollection(user, dealCode);
    }

    @PostMapping("/{dealCode}/ship")
    public DealResponse markInTransit(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.markInTransit(user, dealCode);
    }

    @PostMapping("/{dealCode}/delivered")
    public DealResponse markDelivered(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.markDelivered(user, dealCode);
    }

    @PostMapping("/{dealCode}/confirm-delivery")
    public DealResponse confirmDelivery(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.confirmDelivery(user, dealCode);
    }

    @PostMapping("/{dealCode}/cancel")
    public DealResponse cancelDeal(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.cancelDeal(user, dealCode);
    }

    @PostMapping("/{dealCode}/disputes")
    public DealResponse raiseDispute(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.raiseDispute(user, dealCode);
    }

    @GetMapping("/{dealCode}/tracking")
    public TrackingResult getTracking(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.getTracking(user, dealCode);
    }

    @GetMapping("/{dealCode}/messages")
    public List<Map<String, Object>> getMessages(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode
    ) {
        return dealService.getMessages(user, dealCode);
    }

    @PostMapping("/{dealCode}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode,
        @Valid @RequestBody CreateMessageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.sendMessage(user, dealCode, request));
    }

    @PostMapping("/{dealCode}/ratings")
    public ResponseEntity<Map<String, Object>> rateDeal(
        @AuthenticationPrincipal AppUser user,
        @PathVariable String dealCode,
        @Valid @RequestBody CreateRatingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.rateDeal(user, dealCode, request));
    }
}
