package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SiteAdminInfo {
    private String status;
    private AssetDetails simOrMobile;
    private AssetDetails idCard;
    private AssetDetails lockAndKey;
    private AssetDetails others;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;
}
