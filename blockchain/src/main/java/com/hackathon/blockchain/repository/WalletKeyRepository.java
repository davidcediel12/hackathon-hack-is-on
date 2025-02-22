package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.WalletKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletKeyRepository extends JpaRepository<WalletKey, Long> {
}