package com.kammo.kammobackend.admin;

import com.kammo.kammobackend.deal.DealResponse;
import com.kammo.kammobackend.deal.DealService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DealService dealService;

    public AdminController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping("/disputes")
    public List<DealResponse> getDisputedDeals() {
        return dealService.getDisputedDeals();
    }

    @PostMapping("/disputes/{dealCode}/resolve")
    public DealResponse resolveDispute(
        @PathVariable String dealCode,
        @Valid @RequestBody ResolveDisputeRequest request
    ) {
        return dealService.resolveDispute(dealCode, request.resolution());
    }
}
