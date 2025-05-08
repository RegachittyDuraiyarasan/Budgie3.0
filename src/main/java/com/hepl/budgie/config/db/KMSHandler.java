package com.hepl.budgie.config.db;

import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.ProfileProdCondition;
import com.hepl.budgie.service.KMSHandlerService;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
@Component
@Conditional(ProfileProdCondition.class)
@Slf4j
public class KMSHandler implements KMSHandlerService {

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

    private String encryptionKeyBase64;
    private UUID encryptionKeyUUID;

    public String getEncryptionKeyBase64() {
        return encryptionKeyBase64;
    }

    @Override
    public UUID getEncryptionKeyUUID() {
        return encryptionKeyUUID;
    }

    @Override
    public void buildOrValidateVault() {

        if (doesEncryptionKeyExist()) {
            return;
        }
        DataKeyOptions dataKeyOptions = new DataKeyOptions();
        dataKeyOptions.keyAltNames(Arrays.asList(keyName));

        BsonDocument masterKeyProperties = new BsonDocument();
        masterKeyProperties.put("provider", new BsonString(kmsProvider));
        masterKeyProperties.put("key",
                new BsonString(System.getenv("ARN_KEY")));
        masterKeyProperties.put("region",
                new BsonString("ap-south-1"));
        dataKeyOptions.masterKey(masterKeyProperties);

        BsonBinary dataKeyId = getClientEncryption().createDataKey(kmsProvider, dataKeyOptions);

        this.encryptionKeyUUID = dataKeyId.asUuid();
        log.debug("DataKeyID [UUID]{}", dataKeyId.asUuid());

        String base64DataKeyId = Base64.getEncoder().encodeToString(dataKeyId.getData());
        this.encryptionKeyBase64 = base64DataKeyId;
        log.debug("DataKeyID [base64]: {}", base64DataKeyId);
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

    private Map<String, Map<String, Object>> getKMSMap() {

        Map<String, Object> providerDetails = new HashMap<>();
        providerDetails.put("accessKeyId", new BsonString(System.getenv("ACCESS_KEY_ID")));
        providerDetails.put("secretAccessKey", new BsonString(System.getenv("SECRET_ACCESS_KEY")));

        return Stream.of(
                new AbstractMap.SimpleEntry<>(kmsProvider, providerDetails))
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
    public EncryptOptions getEncryptOptions(AlgorithmType algorithm) {
        EncryptOptions encryptOptions = new EncryptOptions(algorithm.label);
        encryptOptions.keyId(new BsonBinary(getEncryptionKeyUUID()));
        return encryptOptions;
    }

}
