package com.hackathon.blockchain.dto.response;

public record BlockDto(Long id, Long blockIndex, Long timestamp,
                       String previousHash, Long nonce, String hash, Boolean genesis) {
}
