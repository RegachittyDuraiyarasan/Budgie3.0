package com.hepl.budgie.config.db;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.ProfileDevCondition;
import com.hepl.budgie.service.KMSHandlerService;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Component
@Conditional(ProfileDevCondition.class)
@Slf4j
public class DevKMSHandler implements KMSHandlerService {

    @Value(value = "${spring.data.mongodb.uri}")
    private String dbConnection;
    @Value(value = "${spring.data.mongodb.key.vault.database}")
    private String keyVaultDatabase;
    @Value(value = "${spring.data.mongodb.key.vault.collection}")
    private String keyVaultCollection;
    @Value(value = "${spring.data.mongodb.kmsprovider}")
    private String kmsProvider;
    @Value(value = "${spring.data.mongodb.key.vault.name}")
    private String keyName;
    @Value(value = "${spring.data.mongodb.encryption.masterKeyPath}")
    private String masterKeyPath;

    private String encryptionKeyBase64;
    private UUID encryptionKeyUUID;

    public String getEncryptionKeyBase64() {
        return encryptionKeyBase64;
    }

    @Override
    public UUID getEncryptionKeyUUID() {
        return encryptionKeyUUID;
    }

    public void generateCMKIfNotExists() {
        Path path = Paths.get("master-key.txt");
        if (!Files.exists(path)) {
            byte[] localMasterKeyWrite = new byte[96];
            new SecureRandom().nextBytes(localMasterKeyWrite);
            try {
                try (
                        FileOutputStream stream = new FileOutputStream("master-key.txt")) {
                    stream.write(localMasterKeyWrite);
                }
            } catch (IOException e) {
                log.info("Some error occurred while creating cmk");
            }

        }
    }

    private boolean doesEncryptionKeyExist() {

        BsonDocument dek = getClientEncryption().getKeyByAltName(keyName);

        return Optional.ofNullable(dek)
                .map(o -> {
                    log.info("The Document is {}", dek);
                    BsonBinary dataKeyId = o.get("_id").asBinary();
                    this.encryptionKeyUUID = dataKeyId.asUuid();
                    this.encryptionKeyBase64 = Base64.getEncoder().encodeToString(dataKeyId.getData());
                    return true;
                })
                .orElse(false);

    }

    private byte[] getMasterKey() {

        byte[] localMasterKey = new byte[96];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(masterKeyPath);
            fis.read(localMasterKey, 0, 96);
            fis.close();
        } catch (Exception e) {
            log.error("Error Initializing the master key");
        }
        return localMasterKey;
    }

    private Map<String, Map<String, Object>> getKMSMap() {
        Map<String, Object> keyMap = Stream.of(
                new AbstractMap.SimpleEntry<>("key", getMasterKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return Stream.of(
                new AbstractMap.SimpleEntry<>(kmsProvider, keyMap))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public ClientEncryption getClientEncryption() {

        String keyVaultNamespace = keyVaultDatabase + "." + keyVaultCollection;
        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(dbConnection))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(this.getKMSMap())
                .build();

        return ClientEncryptions.create(clientEncryptionSettings);
    }

    @Override
    public void buildOrValidateVault() {
        generateCMKIfNotExists();
        if (doesEncryptionKeyExist()) {
            return;
        }
        DataKeyOptions dataKeyOptions = new DataKeyOptions();
        dataKeyOptions.keyAltNames(Arrays.asList(keyName));

        BsonBinary dataKeyId = getClientEncryption().createDataKey(kmsProvider,
                dataKeyOptions);

        this.encryptionKeyUUID = dataKeyId.asUuid();
        log.debug("DataKeyID [UUID]{}", dataKeyId.asUuid());

        String base64DataKeyId = Base64.getEncoder().encodeToString(dataKeyId.getData());
        this.encryptionKeyBase64 = base64DataKeyId;
        log.debug("DataKeyID [base64]: {}", base64DataKeyId);
    }

    @Override
    public EncryptOptions getEncryptOptions(AlgorithmType algorithm) {
        EncryptOptions encryptOptions = new EncryptOptions(algorithm.label);
        encryptOptions.keyId(new BsonBinary(getEncryptionKeyUUID()));
        return encryptOptions;
    }

}
