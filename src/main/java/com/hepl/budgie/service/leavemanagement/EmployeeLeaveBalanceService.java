package com.hepl.budgie.service.leavemanagement;

import java.util.List;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveBalanceDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;

public interface EmployeeLeaveBalanceService {

	List<LeaveBalanceDTO> getEmployeeLeaveBalance(String empId, String year);

	Object getEmployeeLeaveMaster(String empId, String year, String leaveType, String transactionType);

	void postLeaveTransaction(String empId, FormRequest formRequest);

	List<LeaveTransactions> filterByLeaveTypeAndTransactionType(String empId, String leaveType, String transactionType);

	byte[] exportLeaveData(String empId, FormRequest formRequest);

	Object getEmployeeDetails(String empId);

	Object getEmployeeChartDetails(String empId, String year, String leaveType);

}
