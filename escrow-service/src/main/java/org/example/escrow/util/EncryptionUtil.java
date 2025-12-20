package org.example.escrow.util;


import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility for AES-256 Encryption.
 * Used for securing sensitive fields (like Tax IDs) before storage.
 * Algorithm: AES/GCM/NoPadding (Authenticated Encryption)
 */
@Component
@RequiredArgsConstructor
public class EncryptionUtil {

    private final AppProperties appProperties;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    /**
     * Encrypts a plain string.
     * Output format: Base64(IV + CipherText)
     */
    public String encrypt(String plainText){
        if (plainText == null) return null;

        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            // In production, use SecureRandom to generate unique IVs for every encryption
            new java.security.SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = getSecretKey();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and CipherText
            byte[] validData = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, validData, 0, iv.length);
            System.arraycopy(cipherText, 0, validData, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(validData);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting data", e);
        }
    }
    /**
     * Decrypts a Base64 string.
     * Expects input format: Base64(IV + CipherText)
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            // Extract IV
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // Extract CipherText
            int cipherTextSize = decoded.length - IV_LENGTH_BYTE;
            byte[] cipherText = new byte[cipherTextSize];
            System.arraycopy(decoded, IV_LENGTH_BYTE, cipherText, 0, cipherTextSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey key = getSecretKey();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting data", e);
        }
    }

    private SecretKey getSecretKey(){
        String keyString = appProperties.getSecurity().getEncryptionKey();
        // Decode the Base64 encoded key from properties
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, "AES");
    }
}
