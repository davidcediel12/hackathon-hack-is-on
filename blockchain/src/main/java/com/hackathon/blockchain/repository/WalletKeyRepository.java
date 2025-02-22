package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletKeyRepository extends JpaRepository<WalletKey, Long> {


    Optional<WalletKey> findByWallet(Wallet wallet);

    Optional<WalletKey> findByWalletId(Long walletId);
}