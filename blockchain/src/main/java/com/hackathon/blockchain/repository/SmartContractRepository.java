package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.SmartContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SmartContractRepository extends JpaRepository<SmartContract, Long> {

    Boolean existsByIssuerWalletId(Long issuerWalletId);

    List<SmartContract> findByIssuerWalletId(Long issuerWalletId);
}