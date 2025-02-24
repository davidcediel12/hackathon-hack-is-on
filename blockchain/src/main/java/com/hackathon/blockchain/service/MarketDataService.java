package com.hackathon.blockchain.service;

import java.util.Map;

public interface MarketDataService {
    String getMarketPrice(String symbol);

    double fetchLivePriceForAsset(String symbol);

    Map<String, Double> fetchLiveMarketPrices();
}
