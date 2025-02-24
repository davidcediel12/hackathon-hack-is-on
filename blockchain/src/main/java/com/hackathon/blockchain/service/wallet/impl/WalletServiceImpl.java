package com.hackathon.blockchain.service.wallet.impl;

import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.AssetRepository;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.MarketDataService;
import com.hackathon.blockchain.service.wallet.WalletKeyService;
import com.hackathon.blockchain.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;
import static com.hackathon.blockchain.utils.WalletConstants.ACTIVE_STATUS;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final MarketDataService marketDataService;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final WalletKeyService walletKeyService;

    @Override
    public Optional<Wallet> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    @Transactional
    @Override
    public void initializeLiquidityPools(Map<String, Double> initialAssets) throws NoSuchAlgorithmException, IOException {

        for (Map.Entry<String, Double> entry : initialAssets.entrySet()) {
            String symbol = entry.getKey();
            double initialQuantity = entry.getValue();

            String liquidityWalletAddress = "LP-" + symbol;
            Optional<Wallet> existingWallet = walletRepository.findByAddress(liquidityWalletAddress);

            Wallet savedWallet;

            if (existingWallet.isEmpty()) {
                Wallet liquidityWallet = new Wallet();
                liquidityWallet.setAddress(liquidityWalletAddress);
                liquidityWallet.setBalance(0.0);
                liquidityWallet.setNetWorth(0.0);
                savedWallet = walletRepository.save(liquidityWallet);

                Asset asset = new Asset(null, symbol, initialQuantity, 0.0, liquidityWallet);
                assetRepository.save(asset);

                liquidityWallet.getAssets().add(asset);

                log.info("New liquidity pool wallet and asset for {} saved", symbol);
            } else {
                savedWallet = existingWallet.get();
            }

            if (walletKeyService.getKeysByWallet(savedWallet).isEmpty()) {
                walletKeyService.generateAndStoreKeys(savedWallet);
                log.info("Wallet key for LP-{} created", symbol);
            }
        }
    }

    @Transactional
    @Override
    public String createWalletForUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.USER_NOT_FOUND);

        Optional<Wallet> existingWallet = walletRepository.findByUserId(user.getId());

        if (existingWallet.isPresent()) {
            return "❌ You already have a wallet created.";
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAddress(generateWalletAddress());
        double initialAmount = 100000.0;
        wallet.setBalance(initialAmount);
        wallet.setNetWorth(initialAmount);
        wallet.setAccountStatus(ACTIVE_STATUS);

        walletRepository.save(wallet);

        return "✅ Wallet successfully created! Address: " + wallet.getAddress();
    }

    private String generateWalletAddress() {
        return DigestUtils.sha256Hex(UUID.randomUUID().toString());
    }


    // Ejecuto esta función para tener patrimonios de carteras actualizados continuamente y que no contenga valores estáticos
    @Scheduled(fixedRate = 30000) // Se ejecuta cada 30 segundos
    @Transactional
    public void updateWalletBalancesScheduled() {
        log.info("🔄 Updating wallet net worths based on live market prices...");

        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            double totalValue = 0.0;

            for (Asset asset : wallet.getAssets()) {
                double marketPrice = marketDataService.fetchLivePriceForAsset(asset.getSymbol());
                double assetValue = asset.getQuantity() * marketPrice;
                totalValue += assetValue;

                log.info("💰 Asset {} - Quantity: {} - Market Price: {} - Total Value: {}",
                        asset.getSymbol(), asset.getQuantity(), marketPrice, assetValue);
            }

            if (wallet.getUser() != null) {
                totalValue += wallet.getBalance();
            }

            double previousNetWorth = wallet.getNetWorth();
            wallet.setNetWorth(totalValue);
            walletRepository.save(wallet);

            log.info("📊 Wallet [{}] - Previous Net Worth: {} - Updated Net Worth: {}",
                    wallet.getAddress(), previousNetWorth, totalValue);

            Wallet savedWallet = walletRepository.findById(wallet.getId()).orElse(null);
            if (savedWallet != null) {
                log.info("✅ Confirmed DB Update - Wallet [{}] New Net Worth: {}", savedWallet.getAddress(), savedWallet.getNetWorth());
            } else {
                log.error("❌ Failed to fetch wallet [{}] after update!", wallet.getAddress());
            }
        }

        log.info("✅ All wallet net worths updated successfully!");
    }


    @Override
    public Map<String, Object> getWalletBalance(Long userId) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);

        if (optionalWallet.isEmpty()) {
            throw new ApiException(WALLET_NOT_FOUND, Map.of("error", "Wallet not found"), HttpStatus.NOT_FOUND);
        }

        Wallet wallet = optionalWallet.get();
        Map<String, Double> assetPrices = marketDataService.fetchLiveMarketPrices();

        Map<String, Double> assetsMap = new HashMap<>();
        double netWorth = wallet.getBalance();

        for (Asset asset : wallet.getAssets()) {
            double currentPrice = assetPrices.getOrDefault(asset.getSymbol(), 0.0);
            double assetValue = asset.getQuantity() * currentPrice;
            assetsMap.put(asset.getSymbol(), assetValue);
            netWorth += assetValue;
        }

        Map<String, Object> walletInfo = new HashMap<>();
        walletInfo.put("wallet_address", wallet.getAddress());
        walletInfo.put("cash_balance", wallet.getBalance());
        walletInfo.put("net_worth", netWorth);
        walletInfo.put("assets", assetsMap);

        return walletInfo;
    }
}