package com.hepl.budgie.dto.leave;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import lombok.Data;

@Data
public class LeaveApprovalDTO {

	private String id;
	private String empId;
	private String empName;
	private String appliedTo;
	private String appliedToName;
	private String contactNo;
	private String leaveCode;
	private String category;
	private String leaveType;
	private String assigned;
	private String fromDate;
	private String toDate;
	private String fromSession;
	private String toSession;
	private Object days;
	private Object balanceDay;
	private String empReason;
	private String approverReason;
	private String status;
	private String title;
	private String restrictedHoliday;
	private List<String> appliedCC;
	private List<String> files;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime approveOrRejectDate;
	private Boolean statusCheck;
}
