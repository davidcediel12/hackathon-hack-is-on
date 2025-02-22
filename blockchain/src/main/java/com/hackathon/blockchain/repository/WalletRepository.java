package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByAddress(String address);

    Optional<Wallet> findByUserId(Long userId);
}