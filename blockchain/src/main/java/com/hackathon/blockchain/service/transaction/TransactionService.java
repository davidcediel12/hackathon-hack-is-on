package com.hackathon.blockchain.service.transaction;

public interface TransactionService {


    String buyAsset(Long userId, String symbol, double quantity);

    String sellAsset(Long userId, String symbol, double quantity);
}
