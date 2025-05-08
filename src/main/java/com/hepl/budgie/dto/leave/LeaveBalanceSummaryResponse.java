package com.hepl.budgie.dto.leave;

import lombok.Data;

@Data
public class LeaveBalanceSummaryResponse {

	private String LeaveTypeName;
	private String leaveCode;
	private Double openingBalance = 0.0;
	private Double balance = 0.0;
	private Double granted = 0.0;
	private Double availed = 0.0;
	private Double lapsed = 0.0;
	private Double applied = 0.0;
	private int status;
}
