package com.hackathon.blockchain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssetPurchaseRequest(@NotBlank String symbol,
                                   @NotNull @Positive Double quantity) {
}
