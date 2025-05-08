package com.hepl.budgie.service.leave;

import java.util.List;

import com.hepl.budgie.dto.leave.LeaveBalanceSummaryResponse;

public interface LeaveBalanceService {

	List<LeaveBalanceSummaryResponse> fetchLeaveByYear(String year);

	List<Integer> fetchLeaveByYear();

	Object fetchTransactions(String year, String leaveType);

	Object fetchLeaveCount(String year, String leaveType);

	Object fetchLeaveChart(String year, String leaveType);

	Object fetchSummary(String year, String leaveType);

}
