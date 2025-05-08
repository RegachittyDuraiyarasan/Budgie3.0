package com.hepl.budgie.utils;

import com.hepl.budgie.dto.payroll.PaySheetMonthInfoDTO;
import com.hepl.budgie.dto.payroll.PayrollMonth;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class PayrollMonthMapper {
        public static PaySheetMonthInfoDTO mapToPaySheetMonthInfo(PayrollMonth payrollMonth) {
            YearMonth payrollYearMonth = PayrollDateFormat.getYearMonth(payrollMonth.getPayrollMonth());
            LocalDate payrollDate = payrollYearMonth.atDay(1);
            LocalDate payrollMonthStartDate = payrollYearMonth.atDay(1);
            LocalDate payrollMonthEndDate = payrollYearMonth.atEndOfMonth();

            LocalDate payrollAttStartDate = payrollMonth.getStartDate().toLocalDate();
            LocalDate payrollAttEndDate = payrollMonth.getEndDate().toLocalDate();

            int daysInAMonth = payrollYearMonth.lengthOfMonth();
            YearMonth arrearProcessMonth = PayrollDateFormat.getDaysInMonth(payrollAttStartDate.getYear(), payrollAttStartDate.getMonth());
            LocalDate arrearEndDate = arrearProcessMonth.atEndOfMonth();

            LocalDate firstCycleStart = PayrollDateFormat.stringToYearMonth(payrollMonth.getFromFinYear()).atDay(1);
            LocalDate firstCycleEnd = firstCycleStart.plusMonths(5);
            LocalDate secondCycleStart = firstCycleStart.plusMonths(6);
            LocalDate secondCycleEnd = PayrollDateFormat.stringToYearMonth(payrollMonth.getToFinYear()).atDay(1);

            List<String> firstCycleMonthRange = PayrollDateFormat.getMonthRange(firstCycleStart, firstCycleEnd);
            List<String> secondCycleMonthRange = PayrollDateFormat.getMonthRange(secondCycleStart, secondCycleEnd);
            List<String> startCycleListWithoutCurrentMonth = PayrollDateFormat.getDateRangeWithoutCurrentMonth(payrollDate, firstCycleStart, firstCycleEnd);
            List<String> endCycleListWithoutCurrentMonth = PayrollDateFormat.getDateRangeWithoutCurrentMonth(payrollDate, secondCycleStart, secondCycleEnd);

            return PaySheetMonthInfoDTO.builder()
                    .payrollMonth(payrollMonth.getPayrollMonth())
                    .payrollMonthDays(daysInAMonth)
                    .payrollAttStartDate(payrollAttStartDate)
                    .payrollAttEndDate(payrollAttEndDate)
                    .payrollMonthStartDate(payrollMonthStartDate)
                    .payrollMonthEndDate(payrollMonthEndDate)
                    .arrearProcessMonth(arrearProcessMonth.toString())
                    .arrearEndDate(arrearEndDate)
                    .firstCycleStartDate(firstCycleStart)
                    .firstCycleEndDate(firstCycleEnd)
                    .secondCycleStartDate(secondCycleStart)
                    .secondCycleEndDate(secondCycleEnd)
                    .firstCycleMonthRange(firstCycleMonthRange)
                    .secondCycleMonthRange(secondCycleMonthRange)
                    .startCycleListWithoutCurrentMonth(startCycleListWithoutCurrentMonth)
                    .endCycleListWithoutCurrentMonth(endCycleListWithoutCurrentMonth)
                    .build();

        }

}
