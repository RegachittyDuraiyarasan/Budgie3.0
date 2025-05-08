package com.hepl.budgie.dto.leavemanagement;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.entity.Status;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LeaveTypeRequestDTO {
	
    private String leaveUniqueCode;
    private String leaveTypeName;
    private String leaveTypeCode;
    private String  description;
    private List<String> leaveScheme;
    private String leaveType;
    private String periodicity;
    private Integer periodicityDays;
    private String  encashmentProcess;
    private String  encashmentType;
    private Integer encashMinAccDays;
    private String  status = Status.ACTIVE.label;
    private String  carryForward;
    private Integer maxAccumulationDays;
    @NotBlank(message = "Minimum Leave Availed is required")
    private String minAvailedLimit;
    @NotBlank(message = "Maximum Leave Availed is required")
    private String maxAvailedLimit;
    @NotBlank(message = "Maximum Availed Condition is required")
    private String maxConditionCheck;
    private String maxConditionType;
    private String maxConditionDays;
    @NotBlank(message = "Leave During notice period is required")
    private String leaveDuringNoticePeriod;
    private String numberOfLeaveDuringNoticePeriod;
    private String isBalanceDeductionApplicable;
    private String balanceDeduction;
    @NotBlank(message = "Balance Check while Leave Apply is required")
    private String balanceCheck;


}
