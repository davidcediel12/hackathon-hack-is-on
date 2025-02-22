package com.hackathon.blockchain.dto.response;

public record WalletKeyGenerationResponse(String message, String publicKey,
                                          String absolutePath) {
}
