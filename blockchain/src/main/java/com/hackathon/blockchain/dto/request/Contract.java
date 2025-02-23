package com.hackathon.blockchain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Contract(@NotBlank String name,
                       @NotBlank String conditionExpression,
                       @NotBlank String action,
                       @NotNull Double actionValue,
                       @NotNull Long issuerWalletId) {
}
