package com.hackathon.blockchain.dto.response;

public record ContractResponse(String name,
                               String conditionExpression,
                               String action,
                               Double actionValue,
                               Long issuerWalletId,
                               String digitalSignature) {
}
