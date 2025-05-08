package com.hepl.budgie.dto.leave;

import java.util.Map;

import lombok.Data;

@Data
public class CheckDate {
	 Map<String, Boolean> isAlreadyApplyDates;
	 Map<String, Boolean> weekEndsDates;
	 Map<String, Boolean> holidayDates;
}
