package com.hackathon.blockchain.service.wallet;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.AssetOperationRequest;
import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;

import java.util.Map;

public interface WalletServiceAdapter {
    WalletKeyGenerationResponse generateWalletKeys(String username);

    GenericResponse purchaseAsset(String username, AssetOperationRequest purchaseRequest);
    GenericResponse sellAsset(String username, AssetOperationRequest purchaseRequest);

    Map<String, Object> getWalletBalance(String username);
}
