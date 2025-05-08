package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShiftMasterHeader {

	EMPLOYEE_NO("Employee No", 20), 
	SHIFT_NAME("Shift Name", 20);
	
	private final String value;
	private final int width;
}
