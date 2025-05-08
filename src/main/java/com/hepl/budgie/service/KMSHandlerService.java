package com.hepl.budgie.service;

import com.hepl.budgie.config.db.AlgorithmType;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;

import java.util.UUID;

public interface KMSHandlerService {

    void buildOrValidateVault();

    ClientEncryption getClientEncryption();

    UUID getEncryptionKeyUUID();

    EncryptOptions getEncryptOptions(AlgorithmType algorithm);

}
