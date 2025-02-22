package com.hackathon.blockchain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssetOperationRequest(@NotBlank String symbol,
                                    @NotNull @Positive Double quantity) {
}
