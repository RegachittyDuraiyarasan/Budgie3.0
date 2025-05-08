package com.hepl.budgie.service.leavemanagement;

import java.util.List;

import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;

import jakarta.validation.Valid;

public interface LeaveTransactionTypeService {

	String saveLeaveTransactionType(@Valid LeaveTransactionType leaveTransactionType);

	void deleteLeaveTransactionType(String transactionTypeId);

	List<LeaveTransactionType> fetchLeaveTransactionType();

	List<String> fetchLeaveTransactionTypes();

}
