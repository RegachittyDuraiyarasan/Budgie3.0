package com.hepl.budgie.entity.separation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hepl.budgie.config.auditing.AuditInfo;

import jakarta.annotation.PostConstruct;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SeparationInfo extends AuditInfo {
    @Id
    private String id; 
    private String empId;
    
    private LocalDateTime appliedDate;
    private Boolean isSeparationBankAccNew;
    private SeparationBankAccDetails separationBankAccDetails;
    private String reason;
    private String employeeRemarks;
    private String resignationStatus;
    private int noticePeriod;
    private String accountInfoStatus;
    private ReportingManagerInfo reportingManagerInfo;
    private ReviewerInfo reviewerInfo;
    private ITInfraInfo itInfraInfo;
    private FinanceInfo financeInfo;
    private SiteAdminInfo siteAdminInfo;
    private RelievingDocumentInfo relivingDocumentInfo;
    private HRInfo hrInfo;
    private String noDueStatus;

    @PostConstruct
    public void initDefaults() {
        if (appliedDate == null) appliedDate = LocalDateTime.now();
        if (isSeparationBankAccNew == null) isSeparationBankAccNew = false;
        if (resignationStatus == null) resignationStatus = "Pending";
        if (noDueStatus == null) noDueStatus = "Pending";
    }
    @JsonCreator
public SeparationInfo(
        @JsonProperty("empId") String empId,
        @JsonProperty("appliedDate") LocalDateTime appliedDate,
        @JsonProperty("isSeparationBankAccNew") Boolean isSeparationBankAccNew,
        @JsonProperty("resignationStatus") String resignationStatus,
        @JsonProperty("noDueStatus") String noDueStatus
) {
    this.empId = empId;
    this.appliedDate = appliedDate != null ? appliedDate : LocalDateTime.now();
    this.isSeparationBankAccNew = isSeparationBankAccNew != null ? isSeparationBankAccNew : false;
    this.resignationStatus = resignationStatus != null ? resignationStatus : "Pending";
    this.noDueStatus = noDueStatus != null ? noDueStatus : "Pending";
}

}
