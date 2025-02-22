package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByStatus(String status);

    List<Transaction> findBySenderWallet(Wallet senderWallet);
    List<Transaction> findByReceiverWallet(Wallet receiverWallet);
}