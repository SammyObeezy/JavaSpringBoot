package org.example.escrow.util;

import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for Envelope Encryption (Symmetric).
 * Used for securing sensitive fields (like Tax IDs) before storage.
 * * Pattern:
 * 1. Generate a unique random Data Encryption Key (DEK) for the data.
 * 2. Encrypt the Data using the DEK.
 * 3. Encrypt the DEK using the Application Master Key (KEK).
 * 4. Store blob: [Encrypted DEK] + [Encrypted Data].
 */
@Component
@RequiredArgsConstructor
public class EncryptionUtil {

    private final AppProperties appProperties;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    public String encrypt(String plainText) {
        if (plainText == null) return null;

        try {
            // 1. Generate Ephemeral Data Key (DEK)
            SecretKey dataKey = generateDataKey();

            // 2. Encrypt Data with DEK
            CipherResult dataEncryption = encryptWithKey(plainText.getBytes(StandardCharsets.UTF_8), dataKey);

            // 3. Encrypt DEK with Master Key (KEK)
            SecretKey masterKey = getMasterKey();
            CipherResult keyEncryption = encryptWithKey(dataKey.getEncoded(), masterKey);

            // 4. Pack the Envelope
            // Format: [Len_EncKey (4)] [Key_IV (12)] [Enc_Key (var)] [Data_IV (12)] [Enc_Data (var)]
            ByteBuffer buffer = ByteBuffer.allocate(
                    4 +
                            keyEncryption.iv.length +
                            keyEncryption.cipherText.length +
                            dataEncryption.iv.length +
                            dataEncryption.cipherText.length
            );

            buffer.putInt(keyEncryption.cipherText.length);
            buffer.put(keyEncryption.iv);
            buffer.put(keyEncryption.cipherText);
            buffer.put(dataEncryption.iv);
            buffer.put(dataEncryption.cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting data", e);
        }
    }

    public String decrypt(String encryptedEnvelope) {
        if (encryptedEnvelope == null) return null;

        try {
            byte[] envelopeBytes = Base64.getDecoder().decode(encryptedEnvelope);
            ByteBuffer buffer = ByteBuffer.wrap(envelopeBytes);

            // 1. Unpack Envelope
            int encKeyLength = buffer.getInt();

            byte[] keyIv = new byte[IV_LENGTH_BYTE];
            buffer.get(keyIv);

            byte[] encKeyBytes = new byte[encKeyLength];
            buffer.get(encKeyBytes);

            byte[] dataIv = new byte[IV_LENGTH_BYTE];
            buffer.get(dataIv);

            byte[] encDataBytes = new byte[buffer.remaining()];
            buffer.get(encDataBytes);

            // 2. Decrypt DEK using Master Key (KEK)
            SecretKey masterKey = getMasterKey();
            byte[] dataKeyBytes = decryptWithKey(encKeyBytes, keyIv, masterKey);
            SecretKey dataKey = new SecretKeySpec(dataKeyBytes, "AES");

            // 3. Decrypt Data using DEK
            byte[] plainBytes = decryptWithKey(encDataBytes, dataIv, dataKey);

            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting data", e);
        }
    }

    // --- Helpers ---

    private SecretKey generateDataKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    private CipherResult encryptWithKey(byte[] input, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] cipherText = cipher.doFinal(input);
        return new CipherResult(iv, cipherText);
    }

    private byte[] decryptWithKey(byte[] cipherText, byte[] iv, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        return cipher.doFinal(cipherText);
    }

    private SecretKey getMasterKey() {
        String keyString = appProperties.getSecurity().getEncryptionKey();
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, "AES");
    }

    private record CipherResult(byte[] iv, byte[] cipherText) {}
}