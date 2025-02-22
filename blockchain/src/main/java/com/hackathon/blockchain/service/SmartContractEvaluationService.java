package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.utils.SignatureUtil;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.List;

@Service
public class SmartContractEvaluationService {

    private final SmartContractRepository smartContractRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final WalletKeyService walletKeyService; // Para obtener la clave pública del emisor
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public SmartContractEvaluationService(SmartContractRepository smartContractRepository,
                                          TransactionRepository transactionRepository,
                                          WalletService walletService,
                                          WalletKeyService walletKeyService) {
        this.smartContractRepository = smartContractRepository;
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.walletKeyService = walletKeyService;
    }

    /**
     * Verifica la firma digital del contrato usando la clave pública del emisor.
     */
    public boolean verifyContractSignature(SmartContract contract) {
        try {
            PublicKey issuerPublicKey = walletKeyService.getPublicKeyForWallet(contract.getIssuerWalletId());
            if (issuerPublicKey == null) {
                return false;
            }
            String dataToSign = contract.getName() +
                    contract.getConditionExpression() +
                    contract.getAction() +
                    contract.getActionValue() +
                    contract.getIssuerWalletId();
            return SignatureUtil.verifySignature(dataToSign, contract.getDigitalSignature(), issuerPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Evalúa todos los smart contracts activos sobre las transacciones pendientes.
     * Se inyectan las variables "amount" y "txType" en el contexto de SpEL.
     * Si la condición se cumple y la firma es válida, se ejecuta la acción definida:
     * - Para "CANCEL_TRANSACTION", se marca la transacción como "CANCELED".
     * - (Si hubiera otras acciones, se podrían implementar aquí).
     */
    @Transactional
    public void evaluateSmartContracts() {
        List<SmartContract> contracts = smartContractRepository.findAll(); // O filtrar por "ACTIVE"
        List<Transaction> pendingTxs = transactionRepository.findByStatus("PENDING");

        for (Transaction tx : pendingTxs) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("amount", tx.getAmount());
            context.setVariable("txType", tx.getType());
            for (SmartContract contract : contracts) {
                if (!verifyContractSignature(contract)) continue;
                Expression exp = parser.parseExpression(contract.getConditionExpression());
                Boolean conditionMet = exp.getValue(context, Boolean.class);
                if (conditionMet != null && conditionMet) {
                    if ("CANCEL_TRANSACTION".equalsIgnoreCase(contract.getAction())) {
                        tx.setStatus("CANCELED");
                    } else if ("TRANSFER_FEE".equalsIgnoreCase(contract.getAction())) {
                        walletService.transferFee(tx, contract.getActionValue());
                        tx.setStatus("PROCESSED_CONTRACT");
                    }
                    transactionRepository.save(tx);
                }
            }
        }
    }

    // UNA UNICA CONDICION
    // /**
    //  * Evalúa todos los smart contracts activos sobre las transacciones pendientes.
    //  * Para cada transacción con estado "PENDING", se evalúa la expresión condicional del contrato.
    //  * Si se cumple y la firma es válida, se ejecuta la acción definida (por ejemplo, transferir fee)
    //  * y se actualiza el estado de la transacción a "PROCESSED_CONTRACT".
    //  */
    // @Transactional
    // public void evaluateSmartContracts() {
    //     List<SmartContract> contracts = smartContractRepository.findByStatus("ACTIVE");
    //     // Obtén todas las transacciones pendientes.
    //     List<Transaction> pendingTxs = transactionRepository.findByStatus("PENDING");

    //     for (Transaction tx : pendingTxs) {
    //         // Creamos un contexto de evaluación y definimos variables que se puedan usar en la expresión.
    //         StandardEvaluationContext context = new StandardEvaluationContext();
    //         context.setVariable("amount", tx.getAmount());
    //         // Puedes inyectar otras variables según convenga.

    //         for (SmartContract contract : contracts) {
    //             // Primero, verificar la firma del contrato.
    //             if (!verifyContractSignature(contract)) {
    //                 // Si la firma no es válida, se ignora este contrato.
    //                 continue;
    //             }

    //             // Evaluar la condición del contrato usando SpEL.
    //             Expression exp = parser.parseExpression(contract.getConditionExpression());
    //             Boolean conditionMet = exp.getValue(context, Boolean.class);

    //             if (conditionMet != null && conditionMet) {
    //                 // Si la condición se cumple y la acción es "TRANSFER_FEE", se ejecuta la transferencia.
    //                 if ("TRANSFER_FEE".equalsIgnoreCase(contract.getAction())) {
    //                     walletService.transferFee(tx, contract.getActionValue());
    //                     tx.setStatus("PROCESSED_CONTRACT");
    //                     transactionRepository.save(tx);
    //                 }
    //                 // Aquí se pueden agregar más acciones según el contrato.
    //             }
    //         }
    //     }
    // }
}