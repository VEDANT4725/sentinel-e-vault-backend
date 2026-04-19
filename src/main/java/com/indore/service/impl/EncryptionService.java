package com.indore.service.impl;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

//MainImp

@Service
public class EncryptionService {

    private static final String ALGO = "AES/GCM/NoPadding";

    public String encrypt(String data, String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGO);

            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(data.getBytes());

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData, String base64Key) {
        try {
            byte[] allBytes = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[12];
            byte[] cipherText = new byte[allBytes.length - 12];

            System.arraycopy(allBytes, 0, iv, 0, 12);
            System.arraycopy(allBytes, 12, cipherText, 0, cipherText.length);

            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}