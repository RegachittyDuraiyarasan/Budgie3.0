package com.hepl.budgie.mapper;

import org.bson.BsonBinary;
import org.bson.BsonString;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.config.db.AlgorithmType;
import com.hepl.budgie.entity.userinfo.BankDetails;
import com.hepl.budgie.entity.userinfo.BankDetailsEnc;
import com.hepl.budgie.service.KMSHandlerService;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankMapper {

    @Mapping(target = "accountNumber", ignore = true)
    BankDetailsEnc eBankDetailsEnc(BankDetails bankDetails, @Context KMSHandlerService kmsHandlerService);

    @Mapping(target = "accountNumber", source = "accountNumber", qualifiedByName = "decryptDetails")
    BankDetails toBankDetailsDecrypted(BankDetailsEnc bankDetailsEnc, @Context KMSHandlerService kmsHandlerService);

    @Named("decryptDetails")
    public static String decryptAccountNumber(BsonBinary accountNumberBinary,
            @Context KMSHandlerService kmsHandlerService) {
        return kmsHandlerService.getClientEncryption().decrypt(accountNumberBinary).asString().getValue();
    }

    @AfterMapping
    default void setBankDetails(BankDetails bankDetails, @MappingTarget BankDetailsEnc bankDetailsEnc,
            @Context KMSHandlerService kmsHandlerService) {
        bankDetailsEnc.setAccountNumber(
                kmsHandlerService.getClientEncryption().encrypt(new BsonString(bankDetails.getAccountNumber()),
                        kmsHandlerService.getEncryptOptions(AlgorithmType.DETERMINISTIC)));
    }
}
