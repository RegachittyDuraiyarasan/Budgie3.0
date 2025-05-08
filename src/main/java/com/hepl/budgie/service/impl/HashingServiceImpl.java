package com.hepl.budgie.service.impl;

import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.hepl.budgie.service.HashingService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HashingServiceImpl implements HashingService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int ITERATION_COUNT = 100000;
    private static final int TAG_LENGTH = 128;
    private static final String PASSWORD = "SECRET_KEY";

    @Override
    public String encryption(String plainText) throws Exception {
        log.info("encrypting the data: ${}", plainText);

        return encrypt(plainText, PASSWORD);
    }

    @Override
    public String decryption(String encryptedData) throws Exception {
        log.info("decrypting the data: ${}", encryptedData);

        return decrypt(encryptedData, PASSWORD);
    }

    public static String encrypt(String plaintext, String password) throws Exception {
        byte[] salt = generateRandomBytes(16);
        SecretKey keySpec = deriveKeyFromPassword(password, salt);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = generateRandomBytes(12);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(ciphertext) + ":" +
                Base64.getEncoder().encodeToString(iv) + ":" +
                Base64.getEncoder().encodeToString(salt);
    }

    public static String decrypt(String ciphertext, String password) throws Exception {
        String[] parts = ciphertext.split(":");
        byte[] encryptedData = Base64.getDecoder().decode(parts[0]);
        byte[] iv = Base64.getDecoder().decode(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);

        SecretKey keySpec = deriveKeyFromPassword(password, salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData);
    }

    private static SecretKey deriveKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static byte[] generateRandomBytes(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
