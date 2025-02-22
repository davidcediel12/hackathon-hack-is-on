package com.hackathon.blockchain.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class PemUtil {

    private static final String HEADER_PUBLIC = "-----BEGIN PUBLIC KEY-----\n";
    private static final String HEADER_PRIVATE = "-----BEGIN PRIVATE KEY-----\n";
    private static final String FOOTER_PUBLIC = "\n-----END PUBLIC KEY-----\n";
    private static final String FOOTER_PRIVATE = "\n-----END PRIVATE KEY-----\n";

    private PemUtil() {
    }

    public static String toPEMFormat(PublicKey key) {

        return HEADER_PUBLIC + encode(key.getEncoded()) + FOOTER_PUBLIC;
    }

    public static String toPEMFormat(PrivateKey key) {
        return HEADER_PRIVATE + encode(key.getEncoded()) + FOOTER_PRIVATE;
    }


    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
