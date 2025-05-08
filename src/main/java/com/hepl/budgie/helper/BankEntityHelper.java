package com.hepl.budgie.helper;

import org.bson.BsonBinary;
import org.bson.BsonString;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.userinfo.BankDetails;
import com.hepl.budgie.entity.userinfo.BankDetailsEnc;
import com.hepl.budgie.service.KMSHandlerService;
import com.mongodb.client.model.vault.EncryptOptions;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BankEntityHelper {

    private final KMSHandlerService kmsHandler;

    public static final String DETERMINISTIC_ENCRYPTION_TYPE = "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic";
    public static final String RANDOM_ENCRYPTION_TYPE = "AEAD_AES_256_CBC_HMAC_SHA_512-Random";

    public BankDetailsEnc getEncrypedPerson(BankDetails bankDetails) {

        BankDetailsEnc ep = new BankDetailsEnc();
        ep.setBankName(bankDetails.getBankName());
        ep.setAccountNumber(kmsHandler.getClientEncryption().encrypt(new BsonString(bankDetails.getAccountNumber()),
                getEncryptOptions(DETERMINISTIC_ENCRYPTION_TYPE)));

        return ep;
    }

    public BankDetails getPerson(BankDetailsEnc ep) {

        BankDetails p = new BankDetails();
        p.setBankName(ep.getBankName());
        p.setAccountNumber(kmsHandler.getClientEncryption().decrypt(ep.getAccountNumber()).asString().getValue());
        return p;

    }

    private EncryptOptions getEncryptOptions(String algorithm) {

        EncryptOptions encryptOptions = new EncryptOptions(algorithm);
        encryptOptions.keyId(new BsonBinary(kmsHandler.getEncryptionKeyUUID()));
        return encryptOptions;

    }

}
