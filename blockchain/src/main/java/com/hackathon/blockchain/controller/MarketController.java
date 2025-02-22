package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketDataService marketDataService;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getMarketPrices() {
        return ResponseEntity.ok(marketDataService.fetchLiveMarketPrices());
    }

    @GetMapping("/prices/{symbol}")
    public ResponseEntity<GenericResponse> getMarketPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(new GenericResponse(marketDataService.getMarketPrice(symbol)));
    }


}
