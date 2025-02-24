package com.hackathon.blockchain.service.transaction;

import com.hackathon.blockchain.dto.response.TransactionsDto;

public interface TransactionHistoryService {

    TransactionsDto getTransactions(String username);
}
