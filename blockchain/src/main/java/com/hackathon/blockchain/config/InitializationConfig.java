package com.hackathon.blockchain.config;


import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.service.BlockchainService;
import com.hackathon.blockchain.service.contract.ContractService;
import com.hackathon.blockchain.service.transaction.FeeService;
import com.hackathon.blockchain.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import static com.hackathon.blockchain.utils.AssetConstants.INITIAL_LIQUIDITY_POOL;

@Configuration
@Slf4j
public class InitializationConfig {


    @Bean
    @Transactional
    public ApplicationRunner runner(WalletService walletService,
                                    BlockchainService blockchainService,
                                    ContractService contractService,
                                    SmartContractRepository smartContractRepository,
                                    FeeService feeService) {

        return appArgs -> {
            log.info("Initializing liquidity pools");

            try {
                walletService.initializeLiquidityPools(INITIAL_LIQUIDITY_POOL);
            } catch (Exception e) {
                log.error("Error creating the key for BTC wallet", e);
            }
            log.info(feeService.createFeeWallet());
            log.info("Liquidity pool initialized");
            blockchainService.createGenesisBlock();

            walletService.getWalletByAddress("LP-BTC").ifPresent(wallet -> {
                Boolean contractAlreadyExists = smartContractRepository.existsByIssuerWalletId(wallet.getId());

                if (Boolean.TRUE.equals(contractAlreadyExists)) {
                    return;
                }

                contractService.createContract(new Contract("anti-whale",
                        "#amount > 10 and #txType == 'BUY'",
                        "CANCEL_TRANSACTION", 0.0, wallet.getId()));
                log.info("Contract for liquidity pool BTC created!");
            });

        };

    }
}
