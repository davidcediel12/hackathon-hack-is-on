package com.hackathon.blockchain.service.transaction.impl;

import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.AssetRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.MarketDataService;
import com.hackathon.blockchain.service.contract.SmartContractEvaluationService;
import com.hackathon.blockchain.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static com.hackathon.blockchain.utils.AssetConstants.USDT;
import static com.hackathon.blockchain.utils.MessageConstants.ASSET_PURCHASED_SUCCESSFULLY;
import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;
import static com.hackathon.blockchain.utils.TransactionConstants.BUY_TYPE;
import static com.hackathon.blockchain.utils.TransactionConstants.SELL_TYPE;


@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final WalletRepository walletRepository;
    private final MarketDataService marketDataService;
    private final AssetRepository assetRepository;
    private final SmartContractEvaluationService smartContractEvaluationService;
    private final TransactionRepository transactionRepository;

    /*
     * Los usuarios deben comprar primero USDT para poder cambiar por tokens
     * El dinero fiat no vale para comprar tokens
     * Cuando se intercambia USDT por cualquier moneda, no se añade USDT a los assets de otras monedas
     */
    @Transactional
    @Override
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
            return purchaseUsdt(quantity, userWallet, totalCost, price, usdtLiquidityWallet);
        }
        return purchaseAsset(symbol, quantity, totalCost, userWallet, price, usdtLiquidityWallet, liquidityWallet);

    }

    private String purchaseAsset(String symbol, double quantity,
                                 double totalCost, Wallet userWallet, double price,
                                 Wallet usdtLiquidityWallet, Wallet liquidityWallet) {

        symbol = symbol.toUpperCase();

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

        recordTransaction(liquidityWallet, userWallet, symbol, quantity, price, BUY_TYPE);
        return ASSET_PURCHASED_SUCCESSFULLY;
    }

    private String purchaseUsdt(double quantity, Wallet userWallet,
                                double totalCost, double price, Wallet usdtLiquidityWallet) {

        if (userWallet.getBalance() < totalCost) {
            return "❌ Insufficient fiat balance to buy USDT!";
        }

        userWallet.setBalance(userWallet.getBalance() - totalCost);
        updateWalletAssets(userWallet, USDT, quantity, price);
        updateWalletAssets(usdtLiquidityWallet, USDT, -quantity, price);

        walletRepository.save(userWallet);
        walletRepository.save(usdtLiquidityWallet);

        recordTransaction(usdtLiquidityWallet, userWallet, USDT, quantity, price, BUY_TYPE);
        return "✅ USDT purchased successfully!";
    }

    /*
     * La venta siempre se hace por USDT
     * Los usuarios después pueden cambiar USDT por la moneda fiat
     */
    @Transactional
    @Override
    public String sellAsset(Long userId, String symbol, double quantity) {

        symbol = symbol.toUpperCase();
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        Optional<Wallet> liquidityWalletOpt = walletRepository.findByAddress("LP-" + symbol);

        if (optionalWallet.isEmpty()) return WALLET_NOT_FOUND;
        if (liquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for " + symbol + " not found!";

        Wallet userWallet = optionalWallet.get();
        Wallet liquidityWallet = liquidityWalletOpt.get();

        double price = marketDataService.fetchLivePriceForAsset(symbol);
        double totalRevenue = quantity * price;

        String finalSymbol = symbol;
        Optional<Asset> existingAsset = userWallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(finalSymbol))
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
            String sellError = sellAsset(symbol, quantity, totalRevenue, userWallet, price, liquidityWallet);
            if (sellError != null) return sellError;
        }

        recordTransaction(userWallet, liquidityWallet, symbol, quantity, price, SELL_TYPE);

        walletRepository.save(userWallet);
        walletRepository.save(liquidityWallet);

        return "✅ Asset sold successfully!";
    }


    private String sellAsset(String symbol, double quantity, double totalRevenue, Wallet userWallet, double price, Wallet liquidityWallet) {
        // CASO 2: Venta de otros assets (Recibo USDT)
        Optional<Wallet> usdtLiquidityWalletOpt = walletRepository.findByAddress("LP-USDT");
        if (usdtLiquidityWalletOpt.isEmpty()) {
            return "❌ USDT liquidity pool not found!";
        }
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
        return null;
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

    private void recordTransaction(Wallet sender, Wallet receiver, String assetSymbol,
                                   double quantity, double price, String type) {

        Transaction transaction = Transaction.builder()
                .senderWallet(sender)
                .receiverWallet(receiver)
                .assetSymbol(assetSymbol)
                .amount(quantity)
                .pricePerUnit(price)
                .type(type)
                .timestamp(new Date())
                .status("PENDING")
                .fee(0.0)
                .build();

        smartContractEvaluationService.evaluateSmartContracts(transaction, "LP-" + assetSymbol);
        transactionRepository.save(transaction);
    }
}
