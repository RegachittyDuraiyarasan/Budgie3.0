package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;

import com.hepl.budgie.config.annotation.ValidAssetDetails;
import com.hepl.budgie.entity.YesOrNoEnum;

import lombok.Data;
@Data 
public class FinanceInfo {
    private String status;
    @ValidAssetDetails(status = "status",value = "value",mandatoryCheck = "NO")
    private AssetDetails settlementOfTravel;
    @ValidAssetDetails(status = "status",value = "value",mandatoryCheck = "NO")
    private AssetDetails loanOrSalaryAdvance;
    @ValidAssetDetails(status = "status",value = "value",mandatoryCheck = "NO")
    private AssetDetails otherDues;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;
}
