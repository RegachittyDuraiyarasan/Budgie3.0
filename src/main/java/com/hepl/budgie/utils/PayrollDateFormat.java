package com.hepl.budgie.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PayrollDateFormat {
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String YEAR_DATE_FORMAT = "yyyy-MM-dd";
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";
    public static final String MONTH_YEAR_FORMAT = "MM-yyyy";
    public static LocalDate dateFormat(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }

    public static YearMonth getDaysInMonth(int year, Month month) {
        return YearMonth.of(year, month);
    }
    public static YearMonth stringToYearMonth(String yearMonth) {
        return YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern(PayrollDateFormat.YEAR_MONTH_FORMAT));
    }
    public static YearMonth getYearMonth(String monthYear) {
        return YearMonth.parse(monthYear, DateTimeFormatter.ofPattern(PayrollDateFormat.MONTH_YEAR_FORMAT));
    }
    public static List<String> getMonthRange(LocalDate start, LocalDate end) {
        return  Stream.iterate(YearMonth.from(start), month -> month.plusMonths(1))
                .limit(start.until(end, ChronoUnit.MONTHS) + 1)
                .map(yearMonth -> yearMonth.format(DateTimeFormatter.ofPattern(PayrollDateFormat.MONTH_YEAR_FORMAT)))
                .collect(Collectors.toList());
    }
    public static List<String> getDateRangeWithoutCurrentMonth(LocalDate payrollDate, LocalDate start, LocalDate end) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

        while (!start.isAfter(end)) {
            if (!start.equals(payrollDate)) {
                dates.add(start.format(formatter));
            }
            start = start.plusMonths(1);
        }

        return dates;
    }

}
