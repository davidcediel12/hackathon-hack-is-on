package com.hackathon.blockchain.service.transaction.impl;


import com.hackathon.blockchain.dto.response.TransactionDto;
import com.hackathon.blockchain.dto.response.TransactionsDto;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.wallet.WalletService;
import com.hackathon.blockchain.service.transaction.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final WalletService walletService;
    private final WalletRepository walletRepository;


    @Override
    public TransactionsDto getTransactions(String username) {

        Long walletId = walletRepository.findByUser_Username(username)
                .map(Wallet::getId)
                .orElse(-1L);


        Map<String, List<Transaction>> transactions = walletService.getWalletTransactions(walletId);

        List<Transaction> sentTransactions = transactions.getOrDefault("sent", Collections.emptyList());
        List<TransactionDto> sentTransactionDto = toDto(sentTransactions);

        List<Transaction> receivedTransactions = transactions.getOrDefault("received", Collections.emptyList());
        List<TransactionDto> receivedTransactionDto = toDto(receivedTransactions);

        return new TransactionsDto(sentTransactionDto, receivedTransactionDto);
    }

    private List<TransactionDto> toDto(List<Transaction> transactions) {

        return transactions.stream()
                .map(Transaction::toDto)
                .toList();
    }


}
