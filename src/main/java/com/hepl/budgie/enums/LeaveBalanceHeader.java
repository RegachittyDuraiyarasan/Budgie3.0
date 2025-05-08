package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeaveBalanceHeader {

	EMPLOYEE_NO("Employee No", 20), 
	CL_BALANCE("CL Balance", 20), 
	SL_BALANCE("SL Balance", 20),
	PL_BALANCE("PL Balance", 20), 
	PROB_BALANCE("Prob Balance", 20);

	private final String value;
	private final int width;
}
