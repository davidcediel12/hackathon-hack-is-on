package com.hackathon.blockchain.service.transaction.impl;

import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.transaction.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.hackathon.blockchain.utils.WalletConstants.ACTIVE_STATUS;


@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final WalletRepository walletRepository;

    @Transactional
    @Override
    public void transferFee(Transaction tx, double fee) {
        Wallet sender = tx.getSenderWallet();
        // Supongamos que el liquidity pool de USDT (o la wallet designada para fees) tiene ID 2.
        Optional<Wallet> feeWalletOpt = walletRepository.findByAddress("FEES-USDT");
        if (feeWalletOpt.isPresent()) {
            Wallet feeWallet = feeWalletOpt.get();
            // Actualiza los balances:
            sender.setBalance(sender.getBalance() - fee);
            feeWallet.setBalance(feeWallet.getBalance() + fee);
            walletRepository.save(sender);
            walletRepository.save(feeWallet);
        }
    }


    // Método para crear una wallet para fees (solo USDT)
    @Transactional
    @Override
    public String createFeeWallet() {
        String feeWalletAddress = "FEES-USDT";
        Optional<Wallet> existing = walletRepository.findByAddress(feeWalletAddress);
        if (existing.isPresent()) {
            return "Fee wallet already exists with address: " + feeWalletAddress;
        }
        Wallet feeWallet = new Wallet();
        feeWallet.setAddress(feeWalletAddress);
        feeWallet.setBalance(0.0);
        feeWallet.setNetWorth(0.0);
        feeWallet.setAccountStatus(ACTIVE_STATUS);
        // Al no estar asociada a un usuario, se deja user en null
        walletRepository.save(feeWallet);
        return "Fee wallet created successfully with address: " + feeWalletAddress;
    }
}
