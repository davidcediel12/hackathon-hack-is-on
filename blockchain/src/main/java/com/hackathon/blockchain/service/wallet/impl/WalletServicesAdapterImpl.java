package com.hackathon.blockchain.service.wallet.impl;

import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.service.wallet.WalletServiceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static com.hackathon.blockchain.utils.WalletConstants.KEYS_FOLDER;


@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServicesAdapterImpl implements WalletServiceAdapter {

    private final WalletKeyService walletKeyService;
    private final WalletRepository walletRepository;

    @Override
    public WalletKeyGenerationResponse generateWalletKeys(String username) {

        Wallet wallet = walletRepository.findByUser_Username(username)
                .orElseThrow(() -> new ApiException("Wallet not found", HttpStatus.NOT_FOUND));

        try {
            WalletKey walletKey = walletKeyService.generateAndStoreKeys(wallet);

            return new WalletKeyGenerationResponse(
                    "Keys generated/retrieved successfully for wallet id:" + wallet.getId(),
                    walletKey.getPublicKey(),
                    Path.of(KEYS_FOLDER).toAbsolutePath().toString()
            );

        } catch (NoSuchAlgorithmException | IOException e) {
            String errorMessage = "Unexpected error while creating wallet keys";
            log.error(errorMessage, e);
            throw new ApiException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

}
