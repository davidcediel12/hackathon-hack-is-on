package com.hackathon.blockchain.service.transaction.impl;


import com.hackathon.blockchain.dto.response.TransactionDto;
import com.hackathon.blockchain.dto.response.TransactionsDto;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.transaction.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;


    @Override
    @Transactional(readOnly = true)
    public TransactionsDto getTransactions(String username) {

        Long walletId = walletRepository.findByUser_Username(username)
                .map(Wallet::getId)
                .orElse(-1L);


        Map<String, List<Transaction>> transactions = getWalletTransactions(walletId);

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


    /**
     * Devuelve un mapa con dos listas de transacciones:
     * - "sent": transacciones enviadas (donde la wallet es remitente)
     * - "received": transacciones recibidas (donde la wallet es destinataria)
     */
    private Map<String, List<Transaction>> getWalletTransactions(Long walletId) {
        Optional<Wallet> walletOpt = walletRepository.findById(walletId);
        if (walletOpt.isEmpty()) {
            throw new ApiException(WALLET_NOT_FOUND, Map.of("error", List.of()), HttpStatus.OK);
        }
        Wallet wallet = walletOpt.get();
        List<Transaction> sentTransactions = transactionRepository.findBySenderWallet(wallet);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverWallet(wallet);

        return Map.of(
                "sent", sentTransactions,
                "received", receivedTransactions);
    }

}
