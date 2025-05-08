package com.hepl.budgie.service.leavemanagement;

import java.util.List;
import java.util.Optional;

import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;

public interface LeaveMasterService {

	double employeeLeaveBalance(String empId, String year, String leaveType);
	
	List<LeaveTransactions> fetchLeaveTransactionByYearAndType(String empId, String year, String leaveType);
	
	Optional<LeaveBalanceSummary> getLeaveBalanceSummary(String empId, String year, String leaveType);
}
