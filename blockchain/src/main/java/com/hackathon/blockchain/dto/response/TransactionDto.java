package com.hackathon.blockchain.dto.response;

import java.time.Instant;

public record TransactionDto(Long id, String assetSymbol,
                             Double amount, Double pricePerUnit,
                             String type, Instant timestamp,
                             String status, Double fee,
                             Long senderWalletId, Long receiverWalletId) {
}
