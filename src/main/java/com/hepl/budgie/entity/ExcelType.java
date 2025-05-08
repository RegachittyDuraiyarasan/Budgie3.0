package com.hepl.budgie.entity;

import lombok.Getter;

@Getter
public enum ExcelType {
    CTC_SAMPLE_EXPORT("CTCBreakupSampleExcelExport"),
    IIY_SAMPLE_EXPORT("IIYSampleExcelExport"),
    ADD_EMPLOYEE("AddEmployeeSample"),
    SUPPLEMENTARY_SAMPLE_EXPORT("SuppVariableSampleExcelExport"),
    MONTHLY_IMPORT_SAMPLE_EXPORT("MonthlyImportSampleExcelExport"),
    DAY_TYPE_HISTORY_SAMPLE("AttendanceDayTypeExcel"),
    SHIFT_ROSTER_SAMPLE("ShiftRosterSampleDownload"),
    LEAVE_BALANCE("LeaveBalanceSampleExcel"),
    ATTENDANCE_MUSTER_SAMPLE("AttendanceMusterSampleExcel");

    public final String label;

    private ExcelType(String label) {
        this.label = label;
    }
}
