package com.hepl.budgie.dto.separation;

import lombok.Data;

@Data
public class SeparationReportDTO {
    private String id;
    private String employeeStatus;
    private String reportingManagerStatus;
    private String reviewerStatus;
    private String itInfraStatus;
    private String financeStatus;
    private String accountInfoStatus;
    private String siteAdminStatus;
    private String hrStatus;
    private BasicInfoDto basicInfoDto;
}
