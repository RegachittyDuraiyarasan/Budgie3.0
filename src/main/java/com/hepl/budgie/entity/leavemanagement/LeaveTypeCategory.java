package com.hepl.budgie.entity.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.auditing.AuditInfo;
import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "leave_class_type")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveTypeCategory extends AuditInfo{
	@Id
	private String id;
	private String leaveUniqueCode;
	private String leaveTypeName;
	private String leaveTypeCode;
	private List<LeaveScheme> leaveSchemeId;
	private String leaveType;
	private String periodicity;
	private Integer periodicityDays;
	private String encashmentProcess;
	private String encashmentType;
	private int encashMinAccDays;
	private String status = Status.ACTIVE.label;
	private String description;
	private String carryForward;
	private int maxAccumulationDays;
	private String minAvailedLimit;
	private String maxAvailedLimit;
	private String maxConditionCheck;
	private String maxConditionType;
	private String maxConditionDays;
	private String leaveDuringNoticePeriod;
	private String balanceDeduction;
	private String balanceCheck;

	@Data
	@JsonInclude(JsonInclude.Include.ALWAYS)
	public static class LeaveScheme {
		private String schemeId;
		private String schemeName;
		private String effectiveFrom;
		private String status;
	}
}
