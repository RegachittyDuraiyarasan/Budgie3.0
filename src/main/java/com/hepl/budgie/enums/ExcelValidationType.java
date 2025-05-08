package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExcelValidationType {
    SUPP_VALIDATION("MonthlyVariablesValidation"),
    IIY_ACTIVITY_VALIDATION("IIYActivityValidation"),
    CTC_VALIDATION("CTCBreakupValidation"),
    LEAVE_BALANCE_IMPORT("LeaveBalanceImportValidation"),
    ATTENDANCE_DAY_TYPE("AttendanceDayTypeValidation"),
    ATTENDANCE_SHIFT_ROSTER("AttendanceShiftRosterValidation");

    public final String label;
}
