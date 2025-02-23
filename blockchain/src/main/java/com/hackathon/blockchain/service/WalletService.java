package com.hackathon.blockchain.service;

import com.hackathon.blockchain.exception.ApiException;
import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.AssetRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.contract.SmartContractEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.hackathon.blockchain.utils.AssetConstants.USDT;
import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;
import static com.hackathon.blockchain.utils.WalletConstants.ACTIVE_STATUS;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MarketDataService marketDataService;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final SmartContractEvaluationService smartContractEvaluationService;

    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Optional<Wallet> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    @Transactional
    public void initializeLiquidityPools(Map<String, Double> initialAssets) {

        for (Map.Entry<String, Double> entry : initialAssets.entrySet()) {
            String symbol = entry.getKey();
            double initialQuantity = entry.getValue();

            String liquidityWalletAddress = "LP-" + symbol;
            Optional<Wallet> existingWallet = walletRepository.findByAddress(liquidityWalletAddress);

            if (existingWallet.isEmpty()) {
                Wallet liquidityWallet = new Wallet();
                liquidityWallet.setAddress(liquidityWalletAddress);
                liquidityWallet.setAccountStatus(ACTIVE_STATUS);
                liquidityWallet.setBalance(0.0);
                liquidityWallet.setNetWorth(0.0);
                walletRepository.save(liquidityWallet);

                Asset asset = new Asset(null, symbol, initialQuantity, 0.0, liquidityWallet);
                assetRepository.save(asset);

                liquidityWallet.getAssets().add(asset);
            }
        }
    }

    /*
     * Los usuarios deben comprar primero USDT para poder cambiar por tokens
     * El dinero fiat no vale para comprar tokens
     * Cuando se intercambia USDT por cualquier moneda, no se añade USDT a los assets de otras monedas
     */
    @Transactional
    public String buyAsset(Long userId, String symbol, double quantity) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        Optional<Wallet> liquidityWalletOpt = walletRepository.findByAddress("LP-" + symbol);
        Optional<Wallet> usdtLiquidityWalletOpt = walletRepository.findByAddress("LP-USDT");

        if (optionalWallet.isEmpty()) return WALLET_NOT_FOUND;
        if (liquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for " + symbol + " not found!";
        if (usdtLiquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for USDT not found!";

        Wallet userWallet = optionalWallet.get();
        Wallet liquidityWallet = liquidityWalletOpt.get();
        Wallet usdtLiquidityWallet = usdtLiquidityWalletOpt.get();

        double price = marketDataService.fetchLivePriceForAsset(symbol);
        double totalCost = quantity * price;

        if (symbol.equals(USDT)) {
            if (userWallet.getBalance() < totalCost) {
                return "❌ Insufficient fiat balance to buy USDT!";
            }

            userWallet.setBalance(userWallet.getBalance() - totalCost);
            updateWalletAssets(userWallet, USDT, quantity, price);
            updateWalletAssets(usdtLiquidityWallet, USDT, -quantity, price);

            walletRepository.save(userWallet);
            walletRepository.save(usdtLiquidityWallet);

            recordTransaction(usdtLiquidityWallet, userWallet, USDT, quantity, price, "BUY");
            return "✅ USDT purchased successfully!";
        }

        Optional<Asset> usdtAssetOpt = userWallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(USDT))
                .findFirst();

        if (usdtAssetOpt.isEmpty() || usdtAssetOpt.get().getQuantity() < totalCost) {
            return "❌ Insufficient USDT balance! You must buy USDT first.";
        }

        updateWalletAssets(userWallet, USDT, -totalCost, price);
        updateWalletAssets(usdtLiquidityWallet, USDT, totalCost, price);

        updateWalletAssets(userWallet, symbol, quantity, price);
        updateWalletAssets(liquidityWallet, symbol, -quantity, price);

        walletRepository.save(userWallet);
        walletRepository.save(liquidityWallet);
        walletRepository.save(usdtLiquidityWallet);

        recordTransaction(liquidityWallet, userWallet, symbol, quantity, price, "BUY");

        return "✅ Asset purchased successfully!";
    }

    /*
     * La venta siempre se hace por USDT
     * Los usuarios después pueden cambiar USDT por la moneda fiat
     */
    @Transactional
    public String sellAsset(Long userId, String symbol, double quantity) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        Optional<Wallet> liquidityWalletOpt = walletRepository.findByAddress("LP-" + symbol);

        if (optionalWallet.isEmpty()) return WALLET_NOT_FOUND;
        if (liquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for " + symbol + " not found!";

        Wallet userWallet = optionalWallet.get();
        Wallet liquidityWallet = liquidityWalletOpt.get();

        double price = marketDataService.fetchLivePriceForAsset(symbol);
        double totalRevenue = quantity * price;

        Optional<Asset> existingAsset = userWallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(symbol))
                .findFirst();

        if (existingAsset.isEmpty() || existingAsset.get().getQuantity() < quantity) {
            return "❌ Not enough assets to sell!";
        }

        // CASO 1: Venta de USDT (Recibo dinero fiat)
        if (symbol.equals(USDT)) {
            if (liquidityWallet.getAssets().stream().anyMatch(a -> a.getSymbol().equals(USDT) && a.getQuantity() < quantity)) {
                return "❌ Not enough USDT liquidity!";
            }

            userWallet.setBalance(userWallet.getBalance() + totalRevenue);
            updateWalletAssets(userWallet, symbol, -quantity, price);
            updateWalletAssets(liquidityWallet, symbol, quantity, price);

        } else {
            // CASO 2: Venta de otros assets (Recibo USDT)
            Optional<Wallet> usdtLiquidityWalletOpt = walletRepository.findByAddress("LP-USDT");
            if (usdtLiquidityWalletOpt.isEmpty()) return "❌ USDT liquidity pool not found!";
            Wallet usdtLiquidityWallet = usdtLiquidityWalletOpt.get();

            Optional<Asset> usdtAssetOpt = usdtLiquidityWallet.getAssets().stream()
                    .filter(a -> a.getSymbol().equals(USDT))
                    .findFirst();

            if (usdtAssetOpt.isEmpty() || usdtAssetOpt.get().getQuantity() < totalRevenue) {
                return "❌ Not enough USDT in liquidity pool!";
            }

            updateWalletAssets(userWallet, USDT, totalRevenue, price);
            updateWalletAssets(userWallet, symbol, -quantity, price);
            updateWalletAssets(usdtLiquidityWallet, USDT, -totalRevenue, price);
            updateWalletAssets(liquidityWallet, symbol, quantity, price);

            walletRepository.save(usdtLiquidityWallet);
        }

        recordTransaction(userWallet, liquidityWallet, symbol, quantity, price, "SELL");

        walletRepository.save(userWallet);
        walletRepository.save(liquidityWallet);

        return "✅ Asset sold successfully!";
    }


    private void updateWalletAssets(Wallet wallet, String assetSymbol, double amount,
                                    double purchasedPrice) {
        Optional<Asset> assetOpt = wallet.getAssets().stream()
                .filter(asset -> asset.getSymbol().equalsIgnoreCase(assetSymbol))
                .findFirst();

        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            asset.setQuantity(asset.getQuantity() + amount);
            if (asset.getQuantity() <= 0) {
                wallet.getAssets().remove(asset);
                assetRepository.delete(asset);
            }

            if (amount > 0) {
                asset.setPurchasedPrice(getNewPurchasedPrice(amount, purchasedPrice, asset));
            }

        } else if (amount > 0) {
            Asset newAsset = new Asset();
            newAsset.setSymbol(assetSymbol);
            newAsset.setQuantity(amount);
            newAsset.setWallet(wallet);
            newAsset.setPurchasedPrice(purchasedPrice);

            assetRepository.save(newAsset);
            wallet.getAssets().add(newAsset);

        }
    }

    private static double getNewPurchasedPrice(double amount, double purchasedPrice, Asset asset) {
        return (asset.getPurchasedPrice() * asset.getQuantity() + purchasedPrice * amount) /
                (asset.getQuantity() + amount);
    }

    private void recordTransaction(Wallet sender, Wallet receiver, String assetSymbol, double quantity, double price, String type) {
        Transaction transaction = new Transaction(
                null,             // id (se genera automáticamente)
                sender,           // senderWallet
                receiver,         // receiverWallet
                assetSymbol,      // assetSymbol
                quantity,         // amount
                price,            // pricePerUnit
                type,             // type
                new Date(),       // timestamp
                "PENDING",        // status
                0.0,              // fee
                null              // block (aún no asignado)
        );

        smartContractEvaluationService.evaluateSmartContracts(transaction);
        transactionRepository.save(transaction);
    }


    @Transactional
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

    public Map<String, Object> getWalletBalance(Long userId) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);

        if (optionalWallet.isEmpty()) {
            return Map.of("error", "Wallet not found");
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

    /**
     * Devuelve un mapa con dos listas de transacciones:
     * - "sent": transacciones enviadas (donde la wallet es remitente)
     * - "received": transacciones recibidas (donde la wallet es destinataria)
     */
    public Map<String, List<Transaction>> getWalletTransactions(Long walletId) {
        Optional<Wallet> walletOpt = walletRepository.findById(walletId);
        if (walletOpt.isEmpty()) {
            throw new ApiException(WALLET_NOT_FOUND,
                    Map.of("error", List.of()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Wallet wallet = walletOpt.get();
        List<Transaction> sentTransactions = transactionRepository.findBySenderWallet(wallet);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverWallet(wallet);
        Map<String, List<Transaction>> result = new HashMap<>();
        result.put("sent", sentTransactions);
        result.put("received", receivedTransactions);
        return result;
    }

    // RETO BACKEND

    // Método para transferir el fee: deducirlo del wallet del emisor y sumarlo a la wallet de fees.

}