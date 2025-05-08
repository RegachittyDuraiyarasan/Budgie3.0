package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LeaveReportDTO {
    private String id;
    private String empId;
    private String empName;
    private String appliedTo;
    private String reportingToName;
    private String leaveType;
    private String leaveCategory;
    private Object numOfDays;
    private String fromDate;
    private String toDate;
    private String status;
    private String postedAt;
    private String reason;
    private String remarks;
    private String transactionType;
    private String generateType;
    private String postedOn;
    private String leaveScheme;
    private String sort;
}
