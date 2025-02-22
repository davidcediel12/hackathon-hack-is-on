package com.hackathon.blockchain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final RestClient restClient;

    private static final String API_URL = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-3d8ede30-848f-4a7a-acc2-22ba0cd9a382/default/fake-market-prices";


    public String getMarketPrice(String symbol){
        return "Current price of " + symbol + ": $" + fetchLivePriceForAsset(symbol);
    }
    public double fetchLivePriceForAsset(String symbol) {
        return fetchLiveMarketPrices().getOrDefault(symbol, 0.0d);
    }

    public Map<String, Double> fetchLiveMarketPrices() {

        return restClient.get()
                .uri(API_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Double>>() {
                });
    }
}