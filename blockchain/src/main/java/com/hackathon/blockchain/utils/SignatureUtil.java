package com.hackathon.blockchain.utils;

import com.hackathon.blockchain.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static com.hackathon.blockchain.utils.MessageConstants.WALLET_NOT_FOUND;

@Slf4j
public class SignatureUtil {

    private SignatureUtil() {}

    public static boolean verifySignature(String dataToSign, String digitalSignature,
                                          PublicKey issuerPublicKey) {
        try {
            if (issuerPublicKey == null) {
                throw new ApiException(WALLET_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }

            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(issuerPublicKey);
            publicSignature.update(dataToSign.getBytes(StandardCharsets.UTF_8));


            byte[] signature = Base64.getDecoder().decode(digitalSignature);

            return publicSignature.verify(signature);
        } catch (Exception e) {
            String message = "Something went wrong while validating the contract";
            log.error(message, e);
            throw new ApiException(message, e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
