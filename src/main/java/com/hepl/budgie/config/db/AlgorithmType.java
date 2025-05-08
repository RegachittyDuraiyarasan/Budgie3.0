package com.hepl.budgie.config.db;

public enum AlgorithmType {

    DETERMINISTIC("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic"),
    RANDOM("AEAD_AES_256_CBC_HMAC_SHA_512");

    public final String label;

    private AlgorithmType(String label) {
        this.label = label;
    }

}
