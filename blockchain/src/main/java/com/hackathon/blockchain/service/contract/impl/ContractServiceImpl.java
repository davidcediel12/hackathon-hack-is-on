package com.hackathon.blockchain.service.contract.impl;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.service.contract.ContractService;
import com.hackathon.blockchain.service.wallet.WalletKeyService;
import com.hackathon.blockchain.utils.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final SmartContractRepository smartContractRepository;
    private final WalletKeyService walletKeyService;

    @Override
    public ContractResponse createContract(Contract contract) {


        try {
            PrivateKey key = walletKeyService.getPrivateKeyForWallet(contract.issuerWalletId());

            SmartContract smartContract = SmartContract.builder()
                    .action(contract.action())
                    .actionValue(contract.actionValue())
                    .conditionExpression(contract.conditionExpression())
                    .issuerWalletId(contract.issuerWalletId())
                    .name(contract.name())
                    .status("ACTIVE")
                    .build();

            smartContract.setDigitalSignature(signContractData(smartContract.getDataToSign(), key));

            smartContract = smartContractRepository.save(smartContract);

            return smartContract.toResponse();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {

            String message = "Something went wrong while creating the contract";
            log.error(message, e);
            throw new ApiException(message, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @Override
    public GenericResponse validateContract(Long contractId) {

        SmartContract contract = smartContractRepository.findById(contractId)
                .orElseThrow(() -> new ApiException("Contract not found", HttpStatus.NOT_FOUND));

        try {
            String dataToSign = contract.getDataToSign();

            PublicKey publicKey = walletKeyService.getPublicKeyForWallet(contract.getIssuerWalletId());

            if (publicKey == null) {
                throw new ApiException(WALLET_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            boolean isCorrect = SignatureUtil.verifySignature(dataToSign, contract.getDigitalSignature(), publicKey);

            if (isCorrect) {
                return new GenericResponse("Smart contract is valid");
            }

            return new GenericResponse("Smart contract is invalid");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            String message = "Something went wrong while validating the contract";
            log.error(message, e);
            throw new ApiException(message, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    private String signContractData(String dataToSign, PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {


        if (key == null) {
            throw new ApiException(WALLET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(key);
        privateSignature.update(dataToSign.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(privateSignature.sign());
    }
}
