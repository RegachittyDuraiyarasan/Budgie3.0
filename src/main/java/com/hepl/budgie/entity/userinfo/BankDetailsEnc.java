package com.hepl.budgie.entity.userinfo;

import java.time.ZonedDateTime;

import org.bson.BsonBinary;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankDetailsEnc {

    @Id
    private String id;
    private String bankName;
    private BsonBinary accountNumber;
    private ZonedDateTime date;

}
