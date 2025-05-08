package com.hepl.budgie.dto.payroll;

import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class PayrollCTCBreakupsDTO {
    private String empId;
    private String empName;
    private ZonedDateTime doj;
    private ZonedDateTime withEffectDate;
    private String financialYear;
    private String payrollMonth;
    private Map<String,Integer> earningColumns;
    private Map<String,Integer> deductionColumn;
    private int grossEarnings;
    private int grossDeductions;
    private int netPay;
    private int revisionOrder;
}
