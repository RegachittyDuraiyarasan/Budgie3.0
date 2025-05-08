package com.hepl.budgie.service.impl.excel;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.payroll.PayrollLockMonth.PayrollMonths;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class AttendanceMusterSampleExcel implements ExcelExport {

    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<HeaderList> prepareHeaders() {

        String orgId = jwtHelper.getOrganizationCode();
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate,
                orgId, "IN");
        if (payrollLock == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.PAYROLL_MONTH_NOT_FOUND);
        }
        PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getLockMonth().equals(true))
                .findFirst()
                .orElse(null);
        LocalDate start = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<HeaderList> headerList = new ArrayList<>();
        headerList.add(new HeaderList("Employee", true, "String"));
        headerList.add(new HeaderList("Month", true, "String"));
        LocalDate current = start;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        while (!current.isAfter(end)) {
            String dateStr = current.format(formatter);
            headerList.add(new HeaderList(dateStr, false, "String"));
            current = current.plusDays(1);
        }
        headerList.add(new HeaderList("Total Present", false, "double"));
        headerList.add(new HeaderList("Total Lop", false, "double"));
        headerList.add(new HeaderList("Week Off", false, "double"));
        headerList.add(new HeaderList("Total Holidays", false, "double"));
        headerList.add(new HeaderList("Total Days", false, "double"));
        headerList.add(new HeaderList("Sick Leave", false, "double"));
        headerList.add(new HeaderList("Casual Leave", false, "double"));
        headerList.add(new HeaderList("Privilege Leave", false, "double"));
        headerList.add(new HeaderList("Probational Leave", false, "double"));
        headerList.add(new HeaderList("Total Leave", false, "double"));
        return headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns() {

        String orgId = jwtHelper.getOrganizationCode();
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate,
                orgId, "IN");
        PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getLockMonth().equals(true))
                .findFirst()
                .orElse(null);
        LocalDate start = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<String> dropdownOptions = List.of("P", "A", "Off", "H", "CL", "SL", "PL", "PRL", "LOP");

        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate current = start;
        int columnIndex = 2;

        while (!current.isAfter(end)) {
            // String header = current.format(formatter);
            validation.add(new ExcelBuilder.DropdownConfig(columnIndex, dropdownOptions));
            current = current.plusDays(1);
            columnIndex++;
        }
        return validation;
    }

}
