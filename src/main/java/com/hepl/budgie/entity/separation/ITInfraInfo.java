package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;

import com.hepl.budgie.config.annotation.ValidAssetDetails;

import lombok.Data;

@Data
public class ITInfraInfo {
    private String status;
    @ValidAssetDetails(status = "status",value = "value", mandatoryCheck ="No")
    private AssetDetails desktop;
    @ValidAssetDetails(status = "status",value = "value", mandatoryCheck ="No")
    private AssetDetails laptop;
    @ValidAssetDetails(status = "status",value = "value", mandatoryCheck ="No")
    private AssetDetails mouse;
    @ValidAssetDetails(status = "status",value = "value", mandatoryCheck ="No")
    private AssetDetails bag;
    @ValidAssetDetails(status = "status",value = "value", mandatoryCheck ="No")
    private AssetDetails otherAssets;
    private String emailDeactivation;
    private String sapDeactivation;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;

}
