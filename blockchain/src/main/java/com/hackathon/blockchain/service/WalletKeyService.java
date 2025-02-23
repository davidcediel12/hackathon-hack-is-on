package com.hackathon.blockchain.service;

import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.repository.WalletKeyRepository;
import com.hackathon.blockchain.utils.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import static com.hackathon.blockchain.utils.WalletConstants.KEYS_FOLDER;

@Service
@Slf4j
public class WalletKeyService {


    private final WalletKeyRepository walletKeyRepository;

    public WalletKeyService(WalletKeyRepository walletKeyRepository) {
        this.walletKeyRepository = walletKeyRepository;
        // Asegurarse de que la carpeta /keys exista
        File dir = new File(KEYS_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        log.debug("Directorio de claves: {}", dir.getAbsolutePath());
    }

    public Optional<WalletKey> getKeysByWallet(Wallet wallet) {
        return walletKeyRepository.findByWallet(wallet);
    }

    public Optional<WalletKey> getKeysByWalletId(Long walletId) {
        return walletKeyRepository.findByWalletId(walletId);
    }

    /**
     * Genera un par de claves RSA de 2048 bits, las convierte a PEM y las almacena en archivos,
     * además de guardarlas en la base de datos vinculadas a la wallet.
     */
    public WalletKey generateAndStoreKeys(Wallet wallet) throws NoSuchAlgorithmException, IOException {
        // Generar el par de claves
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Convertir las claves a formato PEM
        String publicKeyPEM = PemUtil.toPEMFormat(keyPair.getPublic());
        String privateKeyPEM = PemUtil.toPEMFormat(keyPair.getPrivate());

        // Guardar las claves en archivos dentro de la carpeta /keys
        Path privateKeyPath = Path.of(KEYS_FOLDER, "wallet_" + wallet.getId() + "_private.pem");
        Path publicKeyPath = Path.of(KEYS_FOLDER, "wallet_" + wallet.getId() + "_public.pem");

        try (FileOutputStream fos = new FileOutputStream(privateKeyPath.toFile())) {
            fos.write(privateKeyPEM.getBytes());
        }
        try (FileOutputStream fos = new FileOutputStream(publicKeyPath.toFile())) {
            fos.write(publicKeyPEM.getBytes());
        }

        // Crear y guardar la entidad WalletKey en la BD
        WalletKey walletKey = new WalletKey();
        walletKey.setWallet(wallet);
        walletKey.setPublicKey(publicKeyPEM);
        walletKey.setPrivateKey(privateKeyPEM);
        return walletKeyRepository.save(walletKey);
    }

    // Método para obtener la clave pública de una wallet (en formato PublicKey)
    public PublicKey getPublicKeyForWallet(Long walletId) {
        Optional<WalletKey> keyOpt = walletKeyRepository.findByWalletId(walletId);

        if (keyOpt.isEmpty()) {
            return null;
        }


        String publicKeyPEM = keyOpt.get().getPublicKey();
        try {
            // Elimina encabezados, pies y saltos de línea
            String publicKeyContent = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("Error obtaining the public key", e);
            return null;
        }

    }

    /**
     * Devuelve la clave privada asociada a la wallet.
     */
    public PrivateKey getPrivateKeyForWallet(Long walletId) {
        Optional<WalletKey> keyOpt = walletKeyRepository.findByWalletId(walletId);

        if (keyOpt.isEmpty()) {
            return null;
        }


        String privateKeyPEM = keyOpt.get().getPrivateKey();
        try {
            String privateKeyContent = privateKeyPEM
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("Error obtaining the private key", e);
            return null;
        }
    }
}