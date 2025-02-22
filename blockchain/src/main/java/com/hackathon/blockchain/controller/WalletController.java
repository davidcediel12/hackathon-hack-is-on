package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.AssetPurchaseRequest;
import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;
import com.hackathon.blockchain.service.WalletService;
import com.hackathon.blockchain.service.wallet.WalletServiceAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletServiceAdapter walletServiceAdapter;

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
                                                    @Valid @RequestBody AssetPurchaseRequest purchaseRequest) {

        return ResponseEntity.ok(walletServiceAdapter.purchaseAsset(authentication.getName(), purchaseRequest));
    }
}
