package com.hackathon.blockchain.service.transaction;

import com.hackathon.blockchain.model.Transaction;

public interface FeeService {
    void transferFee(Transaction tx, double fee);

    String createFeeWallet();
}
