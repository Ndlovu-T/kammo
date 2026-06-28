package com.kammo.kammobackend.wallet;

import com.kammo.kammobackend.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal AppUser user) {
        return walletService.getMyWallet(user);
    }

    @PostMapping("/topup")
    public TopupResponse topup(
        @AuthenticationPrincipal AppUser user,
        @Valid @RequestBody TopupRequest request
    ) {
        return walletService.initiateTopup(user, request.amount());
    }

    @PostMapping("/topup/verify")
    public WalletResponse verifyTopup(
        @AuthenticationPrincipal AppUser user,
        @Valid @RequestBody VerifyTopupRequest request
    ) {
        return walletService.verifyTopup(user, request.reference());
    }
}
