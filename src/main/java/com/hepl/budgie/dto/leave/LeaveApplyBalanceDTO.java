package com.hepl.budgie.dto.leave;

import lombok.Data;

@Data
public class LeaveApplyBalanceDTO {

	private Double leaveBalance = 0.0;
	private String balanceCheck;
	private String minAvailedLimit;
	private String maxAvailedLimit;
	private String maxConditionCheck;
	private String maxConditionType;
	private String maxConditionDays;
	private String leaveDuringNoticePeriod;
}
