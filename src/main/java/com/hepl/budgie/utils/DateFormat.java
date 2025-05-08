package com.hepl.budgie.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormat {

    private DateFormat() {
        throw new IllegalStateException("Utility date format class");
    }

    public static Date date(Date request, String standardDay) {
        LocalDate baseLocalDate = request.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String formattedDate = baseLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-" + standardDay;

        return Date.from(LocalDate.parse(formattedDate)
                .atStartOfDay()
                .atZone(ZoneOffset.UTC)
                .toInstant());
    }
}
