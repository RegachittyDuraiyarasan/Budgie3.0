package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.hepl.budgie.config.annotation.ReportingManagerAttritionValidator;

import lombok.Data;

@Data
@ReportingManagerAttritionValidator(attritionStatus = "attritionStatus",desirableCriteria = "desirableCriteria",desirableRemarks = "desirableRemarks",undesirableCriteria = "undesirableCriteria", undesirableRemarks = "undesirableRemarks"   )
public class ReportingManagerInfo {
    private String reportingManagerId;
    private LocalDateTime approvedOn;
    private String approvedBy ;
    private String remarks;
    private String attritionStatus;
    private List<String> desirableCriteria;
    private String desirableRemarks;
    private List<String> undesirableCriteria;
    private String undesirableRemarks;
    private String rehire;
    private String rehireRemarks;
    private String genericEmail;
    private String emailBackup;
    private String dataBackup;
    private String status;
}
