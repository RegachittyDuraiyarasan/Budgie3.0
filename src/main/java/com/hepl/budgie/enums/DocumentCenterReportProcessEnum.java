package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCenterReportProcessEnum {
    DOCUMENT("Document"),
    COMPANYPOLICY("CompanyPolicy");

    public final String label;
}
