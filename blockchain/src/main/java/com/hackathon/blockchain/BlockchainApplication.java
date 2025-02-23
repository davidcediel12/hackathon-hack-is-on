package com.hackathon.blockchain;

import com.hackathon.blockchain.dto.request.Contract;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.service.BlockchainService;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.service.WalletService;
import com.hackathon.blockchain.service.contract.ContractService;
import com.hackathon.blockchain.service.transaction.FeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import static com.hackathon.blockchain.utils.AssetConstants.INITIAL_LIQUIDITY_POOL;

@SpringBootApplication(scanBasePackages = "com.hackathon.blockchain")
@EnableScheduling
@Slf4j
public class BlockchainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

    @Bean
    @Transactional
    public ApplicationRunner runner(WalletService walletService,
                                    BlockchainService blockchainService,
                                    WalletKeyService walletKeyService,
                                    ContractService contractService,
                                    SmartContractRepository smartContractRepository,
                                    FeeService feeService) {

        return appArgs -> {
            log.info("Initializing liquidity pools");
            walletService.initializeLiquidityPools(INITIAL_LIQUIDITY_POOL);
            log.info(feeService.createFeeWallet());
            log.info("Liquidity pool initialized");
            blockchainService.createGenesisBlock();

            walletService.getWalletByAddress("LP-BTC").ifPresent(wallet -> {
                try {
                    if (walletKeyService.getKeysByWallet(wallet).isEmpty()) {
                        walletKeyService.generateAndStoreKeys(wallet);
                    }
                } catch (Exception e) {
                    log.error("Error creating the key for BTC wallet", e);
                }

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
