package com.hepl.budgie.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.stream.Stream;

public class DateTimeFormatting {

	private DateTimeFormatting() {
		throw new IllegalStateException("Utility Date Time Formatting class");
	}

	/**
	 * Formats a LocalDate object into a string using the specified format.
	 *
	 * @param date   The date to format.
	 * @param format The format pattern (e.g., "yyyy-MM-dd").
	 * @return The formatted date string.
	 */
	public static String formatDate(LocalDate date, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return date.format(formatter);
	}

	/**
	 * Formats a LocalTime object into a string using the specified format.
	 *
	 * @param time   The time to format.
	 * @param format The format pattern (e.g., "HH:mm:ss").
	 * @return The formatted time string.
	 */
	public static String formatTime(LocalTime time, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return time.format(formatter);
	}

	/**
	 * Parses a string into a LocalTime object using the specified format.
	 *
	 * @param timeString The time string to parse.
	 * @param format     The format pattern (e.g., "HH:mm:ss").
	 * @return The parsed LocalTime object.
	 * @throws IllegalArgumentException If the input string cannot be parsed.
	 */
	public static LocalTime parseTime(String timeString, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return LocalTime.parse(timeString.trim(), formatter);
	}

	/**
	 * Parses a string into a LocalTime object using the specified format, with
	 * case-insensitive parsing.
	 *
	 * @param timeString The time string to parse.
	 * @param format     The format pattern (e.g., "hh:mm a").
	 * @return The parsed LocalTime object.
	 * @throws IllegalArgumentException If the input string cannot be parsed.
	 */
	public static LocalTime parseTimeCaseInsensitive(String timeString, String format) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(format)
				.toFormatter(Locale.ENGLISH);
		return LocalTime.parse(timeString.trim(), formatter);
	}

	/**
	 * Calculates the period (difference) between two LocalDate objects.
	 *
	 * @param start The start date.
	 * @param end   The end date.
	 * @return The Period object representing the difference between the two dates.
	 */
	public static Period calculateDateDifference(LocalDate start, LocalDate end) {
		return Period.between(start, end);
	}

	/**
	 * Checks if the date range includes a Saturday or Sunday.
	 */
	public static boolean includesWeekend(LocalDate startDate, LocalDate endDate) {
		return Stream.iterate(startDate, date -> date.plusDays(1))
				.limit(DateDifference.calculateDayDifference(startDate, endDate) + 1).anyMatch(date -> {
					DayOfWeek dayOfWeek = date.getDayOfWeek();
					return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
				});
	}
}