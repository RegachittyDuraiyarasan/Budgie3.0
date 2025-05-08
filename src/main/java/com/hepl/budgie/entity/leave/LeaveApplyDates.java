package com.hepl.budgie.entity.leave;

import lombok.Data;

@Data
public class LeaveApplyDates {

	private String date;
	private String fromSession;
	private String toSession;
	private String leaveType;
	private double count;
	private String status;
	private Boolean isHalfDay;
	private String approverId;
	private String approverStatus;
	private String approverRemarks;
	private String approverAt;
}
