package com.hackathon.blockchain;

import com.hackathon.blockchain.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.hackathon.blockchain.utils.AssetConstants.INITIAL_LIQUIDITY_POOL;

@SpringBootApplication(scanBasePackages = "com.hackathon.blockchain")
@EnableScheduling
@Slf4j
public class BlockchainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner(WalletService walletService) {

        return appArgs -> {
            log.info("Initializing liquidity pools");
            walletService.initializeLiquidityPools(INITIAL_LIQUIDITY_POOL);
            log.info("Liquidity pool initialized");
        };
    }

}
