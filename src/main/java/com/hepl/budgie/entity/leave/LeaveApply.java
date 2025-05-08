package com.hepl.budgie.entity.leave;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "leave_apply")
public class LeaveApply extends AuditInfo {

	@Id
	private String id;
	private String leaveCode;
	private String empId;
	private String appliedTo;
	private String leaveType;
	private String leaveCategory;
	private List<LeaveApplyDates> leaveApply;
	private List<String> dateList;
	private double numOfDays;
	private double balance;
	private String compOffWorkDate;
	private String empReason;
	private String approverReason;
	private String contactNo;
	private String fromDate;
	private String toDate;
	private String fromSession;
	private String toSession;
	private String workDate;
	private LocalDateTime approveOrRejectDate;
	private List<String> appliedCC;
	private List<String> fileNames;
	private String leaveCancel;
	private String status;
}
