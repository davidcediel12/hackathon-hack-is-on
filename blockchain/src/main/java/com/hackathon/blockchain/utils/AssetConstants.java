package com.hackathon.blockchain.utils;

import java.util.Map;

public class AssetConstants {

    private AssetConstants() {
    }


    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USDT = "USDT";
    public static final String NCOIN = "NCOIN";
    public static final String CCOIN = "CCOIN";


    public static final Map<String, Double> INITIAL_LIQUIDITY_POOL = Map.of(
            BTC, 100000.0,
            ETH, 400000.0,
            USDT, 1000000.0,
            NCOIN, 10000000.0,
            CCOIN, 2000000.0
    );
}
