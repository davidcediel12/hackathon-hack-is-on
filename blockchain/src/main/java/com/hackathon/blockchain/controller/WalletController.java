package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.AssetOperationRequest;
import com.hackathon.blockchain.dto.response.TransactionsDto;
import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;
import com.hackathon.blockchain.service.wallet.WalletService;
import com.hackathon.blockchain.service.transaction.TransactionHistoryService;
import com.hackathon.blockchain.service.wallet.WalletServiceAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletServiceAdapter walletServiceAdapter;
    private final TransactionHistoryService transactionHistoryService;


    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createWallet(Authentication authentication) {

        String walletMessage = walletService.createWalletForUser(authentication.getName());

        return ResponseEntity.ok(new GenericResponse(walletMessage));
    }


    @PostMapping("/generate-keys")
    public ResponseEntity<WalletKeyGenerationResponse> generateKeys(Authentication authentication) {

        return ResponseEntity.ok(walletServiceAdapter.generateWalletKeys(authentication.getName()));
    }


    @PostMapping("/buy")
    public ResponseEntity<GenericResponse> buyAsset(Authentication authentication,
                                                    @Valid @RequestBody AssetOperationRequest purchaseRequest) {

        return ResponseEntity.ok(walletServiceAdapter.purchaseAsset(authentication.getName(), purchaseRequest));
    }


    @PostMapping("/sell")
    public ResponseEntity<GenericResponse> sellAsset(Authentication authentication,
                                                     @Valid @RequestBody AssetOperationRequest sellRequest) {

        return ResponseEntity.ok(walletServiceAdapter.sellAsset(authentication.getName(), sellRequest));
    }

//    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getWalletBalance(Authentication authentication) {

        return ResponseEntity.ok(walletServiceAdapter.getWalletBalance(authentication.getName()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<TransactionsDto> getTransactions(Authentication authentication) {
        return ResponseEntity.ok(transactionHistoryService.getTransactions(authentication.getName()));
    }
}
