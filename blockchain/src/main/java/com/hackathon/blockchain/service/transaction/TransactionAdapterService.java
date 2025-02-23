package com.hackathon.blockchain.service.transaction;

import com.hackathon.blockchain.dto.response.TransactionsDto;

public interface TransactionAdapterService {

    TransactionsDto getTransactions(String username);
}
