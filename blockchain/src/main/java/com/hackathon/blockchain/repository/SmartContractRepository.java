package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.SmartContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmartContractRepository extends JpaRepository<SmartContract, Long> {
}