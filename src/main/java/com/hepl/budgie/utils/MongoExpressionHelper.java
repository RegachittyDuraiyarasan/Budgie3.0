package com.hepl.budgie.utils;

import java.util.TimeZone;

import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone;

public class MongoExpressionHelper {

    private MongoExpressionHelper() {
        throw new IllegalStateException("Mongo Expression helper");
    }

    public static DateOperators.Month dateOperatorMonth(String field, TimeZone timezone) {
        DateOperators.Timezone operatorTimeZone = DateOperators.Timezone.fromOffset(timezone);
        return DateOperators.Month.monthOf(field).withTimezone(operatorTimeZone);
    }

    public static DateOperators.DayOfMonth dateOperatorDayOfMonth(String field, TimeZone timezone) {
        DateOperators.Timezone operatorTimeZone = DateOperators.Timezone.fromOffset(timezone);
        return DateOperators.DayOfMonth.dayOfMonth(field).withTimezone(operatorTimeZone);
    }

    public static DateOperators.DateToString dateToString(String field, TimeZone timezone) {
        return DateOperators.DateToString.dateOf(field)
                .toString("%d-%m-%Y").withTimezone(Timezone.fromOffset(timezone));
    }

    public static DateOperators.DateToString dateToString(String field, String format, TimeZone timezone) {
        return DateOperators.DateToString.dateOf(field)
                .toString(format).withTimezone(Timezone.fromOffset(timezone));
    }

    public static DateOperators.DateDiff dateDiff(String startDateRef, String endDateRef, String unit) {
        return DateOperators.DateDiff.diffValueOf(startDateRef, unit).toDateOf(endDateRef);
    }
}
