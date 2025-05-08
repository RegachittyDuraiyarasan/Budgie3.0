package com.hepl.budgie.entity.leavemanagement;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "leave_master")
@NoArgsConstructor
@AllArgsConstructor
public class LeaveMaster extends AuditInfo{

	@Id
	private String id;
	private String empId;
	private String year;
	private List<LeaveBalanceSummary> leaveBalanceSummary;
	private List<LeaveTransactions> leaveTransactions;
}
