package com.hackathon.blockchain.service.contract;

import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.transaction.FeeService;
import com.hackathon.blockchain.service.wallet.WalletKeyService;
import com.hackathon.blockchain.utils.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartContractEvaluationService {

    public static final String ACTIVE_STATUS = "ACTIVE";
    private final SmartContractRepository smartContractRepository;
    private final WalletKeyService walletKeyService;
    private final FeeService feeService;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final WalletRepository walletRepository;


    /**
     * Verifica la firma digital del contrato usando la clave pública del emisor.
     */
    public boolean verifyContractSignature(SmartContract contract) {
        try {
            PublicKey issuerPublicKey = walletKeyService.getPublicKeyForWallet(contract.getIssuerWalletId());
            if (issuerPublicKey == null) {
                return false;
            }
            String dataToSign = contract.getDataToSign();
            return SignatureUtil.verifySignature(dataToSign, contract.getDigitalSignature(), issuerPublicKey);
        } catch (Exception e) {
            log.error("Error while verifying contract signature", e);
            return false;
        }
    }

    @Transactional
    public void evaluateSmartContracts(Transaction transaction, String liquidityPoolAddress) {
        Long walletId = walletRepository.findByAddress(liquidityPoolAddress)
                .map(Wallet::getId)
                .orElse(0L);

        List<SmartContract> contracts = smartContractRepository.findByStatusAndIssuerWalletId(
                ACTIVE_STATUS, walletId);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("amount", transaction.getAmount());
        context.setVariable("txType", transaction.getType());


        for (SmartContract contract : contracts) {
            if (!verifyContractSignature(contract)) {
                continue;
            }
            Expression exp = parser.parseExpression(contract.getConditionExpression());
            Boolean conditionMet = exp.getValue(context, Boolean.class);

            if (Objects.equals(Boolean.TRUE, conditionMet)) {
                if ("CANCEL_TRANSACTION".equalsIgnoreCase(contract.getAction())) {
                    throw new ApiException(
                            "❌ Transaction blocked by smart contract conditions for " + transaction.getAssetSymbol(),
                            HttpStatus.BAD_REQUEST);

                } else if ("TRANSFER_FEE".equalsIgnoreCase(contract.getAction())) {
                    feeService.transferFee(transaction, contract.getActionValue());
                    transaction.setFee(contract.getActionValue());
                }
            }
        }
    }
}