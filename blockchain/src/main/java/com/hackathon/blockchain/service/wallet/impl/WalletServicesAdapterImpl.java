package com.hackathon.blockchain.service.wallet.impl;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.AssetOperationRequest;
import com.hackathon.blockchain.dto.response.WalletKeyGenerationResponse;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.service.WalletService;
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
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

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

    @Override
    public GenericResponse purchaseAsset(String username, AssetOperationRequest purchaseRequest) {

        User user = getUser(username);

        String message = walletService.buyAsset(user.getId(), purchaseRequest.symbol(), purchaseRequest.quantity());

        return new GenericResponse(message);
    }

    @Override
    public GenericResponse sellAsset(String username, AssetOperationRequest purchaseRequest) {

        User user = getUser(username);

        String message = walletService.sellAsset(user.getId(), purchaseRequest.symbol(), purchaseRequest.quantity());

        return new GenericResponse(message);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.USER_NOT_FOUND);
    }

}
