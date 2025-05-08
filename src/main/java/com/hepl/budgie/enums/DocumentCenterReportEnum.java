package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCenterReportEnum {

    ACCEPT("Accept"),
    DECLINE("Decline"),
    DOWNLOAD("Download");

    public final String label;
}
