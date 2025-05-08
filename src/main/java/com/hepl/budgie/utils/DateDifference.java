package com.hepl.budgie.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

@Component
public class DateDifference {

    /**
     * Calculates the number of days between two dates.
     *
     * @param start The start date.
     * @param end   The end date.
     * @return The number of days between the start and end dates.
     */
    public static long calculateDayDifference(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the number of months between two dates.
     *
     * @param start The start date.
     * @param end   The end date.
     * @return The number of months between the start and end dates.
     */
    public static long calculateMonthDifference(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Finds the date after a specified number of days, excluding Sundays.
     *
     * @param startDate The starting date.
     * @param daysToAdd The number of days to add.
     * @return The resulting date after skipping Sundays.
     */
    public static LocalDate findDateAfterDaysExcludingSundays(LocalDate startDate, int daysToAdd) {
        LocalDate currentDate = startDate;
        int daysAdded = 0;

        while (daysAdded < daysToAdd) {
            currentDate = currentDate.plusDays(1);

            // Skip Sundays
            if (currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                daysAdded++;
            }
        }

        return currentDate;
    }
}
