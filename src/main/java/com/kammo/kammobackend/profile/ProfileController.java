package com.kammo.kammobackend.profile;

import com.kammo.kammobackend.deal.Deal;
import com.kammo.kammobackend.deal.DealRepository;
import com.kammo.kammobackend.deal.DealResponse;
import com.kammo.kammobackend.deal.DealStatus;
import com.kammo.kammobackend.user.AppUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final DealRepository dealRepository;

    public ProfileController(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @GetMapping("/me/profile")
    public Map<String, Object> getProfile(@AuthenticationPrincipal AppUser user) {
        List<Deal> deals = dealRepository.findByOwnerUserIdOrderByCreatedAtDesc(user.getId());
        BigDecimal totalVolume = deals.stream()
            .map(Deal::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long disputes = deals.stream().filter(deal -> deal.getStatus() == DealStatus.DISPUTED).count();

        return Map.ofEntries(
            Map.entry("kammoId", user.getKammoId()),
            Map.entry("phoneNumber", user.getPhoneNumber()),
            Map.entry("email", user.getEmail()),
            Map.entry("displayName", user.getEmail().split("@")[0]),
            Map.entry("username", "@" + user.getEmail().split("@")[0].replaceAll("[^A-Za-z0-9_]", "_")),
            Map.entry("trustScore", disputes == 0 ? 98 : 85),
            Map.entry("rating", 4.9),
            Map.entry("dealCount", deals.size()),
            Map.entry("volume", totalVolume),
            Map.entry("disputeCount", disputes),
            Map.entry("memberSince", user.getCreatedAt())
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(@AuthenticationPrincipal AppUser user) {
        List<Deal> deals = dealRepository.findByOwnerUserIdOrderByCreatedAtDesc(user.getId());
        BigDecimal activeValue = deals.stream()
            .filter(deal -> deal.getStatus() != DealStatus.COMPLETED && deal.getStatus() != DealStatus.DISPUTED)
            .map(Deal::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
            "activeDealValue", activeValue,
            "activeDealCount", deals.stream().filter(deal -> deal.getStatus() != DealStatus.COMPLETED).count(),
            "securedCount", deals.stream().filter(deal -> deal.getStatus() == DealStatus.PAYMENT_SECURED).count(),
            "inTransitCount", deals.stream().filter(deal -> deal.getStatus() == DealStatus.IN_TRANSIT).count(),
            "disputedCount", deals.stream().filter(deal -> deal.getStatus() == DealStatus.DISPUTED).count(),
            "recentDeals", deals.stream().limit(3).map(DealResponse::from).toList()
        );
    }
}
