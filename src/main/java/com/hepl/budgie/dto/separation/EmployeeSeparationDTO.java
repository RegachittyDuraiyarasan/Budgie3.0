package com.hepl.budgie.dto.separation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.annotation.ValidSeparationBankAcc;
import com.hepl.budgie.entity.separation.FinanceInfo;
import com.hepl.budgie.entity.separation.HRInfo;
import com.hepl.budgie.entity.separation.ITInfraInfo;
import com.hepl.budgie.entity.separation.RelievingDocumentInfo;
import com.hepl.budgie.entity.separation.ReportingManagerInfo;
import com.hepl.budgie.entity.separation.ReviewerInfo;
import com.hepl.budgie.entity.separation.SeparationBankAccDetails;
import com.hepl.budgie.entity.separation.SiteAdminInfo;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidSeparationBankAcc(isSeparationBankAccNew = "isSeparationBankAccNew", separationBankAccDetails = "separationBankAccDetails", message = "separationBankAccDetails should be provided only when isSeparationBankAccNew is true.")
public class EmployeeSeparationDTO {
    private String id;
    private String empId;
    private Boolean isSeparationBankAccNew;
    private SeparationBankAccDetails separationBankAccDetails;
    private String reason;
    private String employeeRemarks;
    private String accountInfoStatus;
    @Valid
    private ReportingManagerInfo reportingManagerInfo;
    @Valid
    private ReviewerInfo reviewerInfo;
    @Valid
    private ITInfraInfo itInfraInfo;
    @Valid
    private FinanceInfo financeInfo;
    private SiteAdminInfo siteAdminInfo;
    private RelievingDocumentInfo relivingDocumentInfo;
    private HRInfo hrInfo;
    private String noDueStatus;
    private String resignationStatus;
    // DOUBT TO BE REMOVED
    private String appliedDate;
    private LocalDate relievingDate;
    // ----
    private BasicInfoDto basicInfoDto;
}
