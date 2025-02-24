package com.hackathon.blockchain.service.wallet;

import com.hackathon.blockchain.model.Wallet;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

public interface WalletService {
    Optional<Wallet> getWalletByAddress(String address);

    void initializeLiquidityPools(Map<String, Double> initialAssets) throws NoSuchAlgorithmException, IOException;

    String createWalletForUser(String username);

    Map<String, Object> getWalletBalance(Long userId);
}
