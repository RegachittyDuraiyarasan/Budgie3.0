package com.hepl.budgie.enums;

import com.itextpdf.commons.bouncycastle.asn1.pkcs.IPrivateKeyInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AgeTypeEnum {
    BELOW_60("Below 60"),
    BETWEEN_60_80("60 To 80"),
    ABOVE_80("Above 80");

    private final String label;

}
