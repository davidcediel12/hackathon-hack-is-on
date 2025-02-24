package com.hackathon.blockchain.service.wallet;

import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

public interface WalletKeyService {
    Optional<WalletKey> getKeysByWallet(Wallet wallet);

    WalletKey generateAndStoreKeys(Wallet wallet) throws NoSuchAlgorithmException, IOException;

    PublicKey getPublicKeyForWallet(Long walletId);

    PrivateKey getPrivateKeyForWallet(Long walletId);
}
