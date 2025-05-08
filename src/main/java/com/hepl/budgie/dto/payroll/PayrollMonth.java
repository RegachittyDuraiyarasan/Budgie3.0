package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.PayrollDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollMonth {
    private String finYear;
    private String fromFinYear;
    private String toFinYear;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String payrollMonth;
    private Boolean lockMonth;
    private Boolean payslip;
    private Boolean mail;

    public ZonedDateTime getFormattedMonth() {
        if (payrollMonth == null || payrollMonth.isEmpty()) {
            return null;
        }
        LocalDate parsedDate = LocalDate.parse("01-" + payrollMonth, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        ZoneId zoneId = ZoneId.of(LocaleContextHolder.getTimeZone().getID());
        return parsedDate.atStartOfDay(zoneId);
    }
}
