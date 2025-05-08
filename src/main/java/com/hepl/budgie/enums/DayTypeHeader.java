package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayTypeHeader {

	EMPLOYEE_ID("Employee Id", 20), 
	DATE("Date", 20), 
	DAY_TYPE("Day Type", 20),
	SHIFT_CODE("Shift Code", 20);

	private final String value;
	private final int width;
}
