package com.hackathon.blockchain.dto.response;

import java.util.Date;

public record TransactionDto(Long id, String assetSymbol,
                             Double amount, Double pricePerUnit,
                             String type, Date timestamp,
                             String status, Double fee,
                             Long senderWalletId, Long receiverWalletId) {
}
