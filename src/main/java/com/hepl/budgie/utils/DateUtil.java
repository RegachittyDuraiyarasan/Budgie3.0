package com.hepl.budgie.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {
    public static final String MONTH_FORMAT = "MM-yyyy";
    public static final String ONE_MONTH_PERIOD = "P1M";

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class - instantiation not allowed");
    }

    public static ZonedDateTime parseDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZoneId.systemDefault());
    }

    public static ZonedDateTime parseDate(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static List<String> createDatePeriods(LocalDate startDate, String period, LocalDate endDate) {
        List<String> periods = new ArrayList<>();
        periods.add(startDate.format(DateTimeFormatter.ofPattern(MONTH_FORMAT))); // Add the initial start date

        Period periodObj = Period.parse(period);
        LocalDate currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            currentDate = currentDate.plus(periodObj);
            if (!currentDate.equals(endDate)) {
                periods.add(currentDate.format(DateTimeFormatter.ofPattern(MONTH_FORMAT)));
            }
        }

        return periods;
    }


}
