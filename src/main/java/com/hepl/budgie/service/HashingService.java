package com.hepl.budgie.service;

public interface HashingService {

    String encryption(String plainText) throws Exception;

    String decryption(String encryptedData) throws Exception;
    
}
