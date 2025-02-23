package com.hackathon.blockchain.service.contract.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.dto.response.ContractResponse;
import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.service.contract.ContractService;
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
    private final ObjectMapper objectMapper;

    @Override
    public ContractResponse createContract(Contract contract) {


        try {
            PrivateKey key = walletKeyService.getPrivateKeyForWallet(contract.issuerWalletId());
            String digitalSignature = signContractData(contract, key);

            SmartContract smartContract = SmartContract.builder()
                    .action(contract.action())
                    .actionValue(contract.actionValue())
                    .conditionExpression(contract.conditionExpression())
                    .digitalSignature(digitalSignature)
                    .issuerWalletId(contract.issuerWalletId())
                    .name(contract.name())
                    .build();

            smartContract = smartContractRepository.save(smartContract);
            return smartContract.toResponse();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {

            String message = "Something went wrong while creating contract";
            log.error(message, e);
            throw new ApiException(message, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    private String signContractData(Contract contract, PrivateKey key)
            throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        String contractData;
        contractData = objectMapper.writeValueAsString(contract);

        if (key == null) {
            throw new ApiException(WALLET_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(key);
        privateSignature.update(contractData.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(privateSignature.sign());
    }
}
