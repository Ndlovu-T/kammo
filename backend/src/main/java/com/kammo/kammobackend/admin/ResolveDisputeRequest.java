package com.kammo.kammobackend.admin;

import com.kammo.kammobackend.deal.DisputeResolution;
import jakarta.validation.constraints.NotNull;

public record ResolveDisputeRequest(
    @NotNull DisputeResolution resolution,
    String note
) {
}
