package com.hepl.budgie.dto.payroll;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
public class PaySheetMonthInfoDTO {
    private String payrollMonth;
    private int payrollMonthDays;
    private LocalDate payrollAttStartDate;
    private LocalDate payrollAttEndDate;
    private LocalDate payrollMonthStartDate;
    private LocalDate payrollMonthEndDate;
    private String arrearProcessMonth;
    private LocalDate arrearEndDate;
    private LocalDate firstCycleStartDate;
    private LocalDate firstCycleEndDate;
    private LocalDate secondCycleStartDate;
    private LocalDate secondCycleEndDate;
    private List<String> firstCycleMonthRange;
    private List<String> secondCycleMonthRange;
    private List<String> startCycleListWithoutCurrentMonth;
    private List<String> endCycleListWithoutCurrentMonth;


}
