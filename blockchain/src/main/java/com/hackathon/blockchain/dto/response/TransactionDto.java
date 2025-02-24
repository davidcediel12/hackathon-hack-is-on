package com.hackathon.blockchain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record TransactionDto(Long id, String assetSymbol,
                             Double amount, Double pricePerUnit,
                             String type,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
                             Instant timestamp,
                             String status, Double fee,
                             Long senderWalletId, Long receiverWalletId) {
}
