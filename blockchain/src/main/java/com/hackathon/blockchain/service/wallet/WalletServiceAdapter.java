package com.hackathon.blockchain.service.wallet;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.AssetPurchaseRequest;
import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;

public interface WalletServiceAdapter {
    WalletKeyGenerationResponse generateWalletKeys(String username);

    GenericResponse purchaseAsset(String username, AssetPurchaseRequest purchaseRequest);
}
