package com.hackathon.blockchain.service.wallet;

import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;

public interface WalletServiceAdapter {
    WalletKeyGenerationResponse generateWalletKeys(String username);
}
