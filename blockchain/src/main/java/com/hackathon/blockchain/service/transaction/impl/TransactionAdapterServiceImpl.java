package com.hackathon.blockchain.service.transaction.impl;


import com.hackathon.blockchain.dto.response.TransactionDto;
import com.hackathon.blockchain.dto.response.TransactionsDto;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.WalletService;
import com.hackathon.blockchain.service.transaction.TransactionAdapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionAdapterServiceImpl implements TransactionAdapterService {

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
                .map(this::toDto)
                .toList();
    }


    private TransactionDto toDto(Transaction transaction) {

        return new TransactionDto(transaction.getId(), transaction.getAssetSymbol(),
                transaction.getAmount(), transaction.getPricePerUnit(), transaction.getType(),
                transaction.getTimestamp(), transaction.getStatus(), transaction.getFee(),
                transaction.getSenderWallet().getId(), transaction.getReceiverWallet().getId());
    }
}
