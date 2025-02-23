package com.hackathon.blockchain.dto.response;

import java.time.OffsetDateTime;

public record TransactionDto(Long id, String assetSymbol,
                             Double amount, Double pricePerUnit,
                             String type, OffsetDateTime timestamp,
                             String status, Double fee,
                             Long senderWalletId, Long receiverWalletId) {
}
