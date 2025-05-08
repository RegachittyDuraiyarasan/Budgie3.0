package com.hepl.budgie.service.impl.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.service.impl.leavemanagement.LeaveBalanceImportServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class LeaveBalanceSampleExcel implements ExcelExport {

    private final LeaveBalanceImportServiceImpl leaveBalanceImportService;
    private final JWTHelper jwtHelper;

    @Override
    public List<HeaderList> prepareHeaders() {
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("Period", true, "String"),
                new HeaderList("Year", true, "String"),
                new HeaderList("Months", true, "String"))
                .toList());
        List<String> leaveTypeMap = leaveBalanceImportService.getLeaveTypeMap();
        for (String leaveType : leaveTypeMap) {
            headerList.add(new HeaderList(leaveType, true, "double"));
        }

        return headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns() {

        String orgId = jwtHelper.getOrganizationCode();
        List<String> headers = prepareHeaders().stream().map(HeaderList::getHeader).toList();
        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();

        List<String> period = List.of(
                "Year", "Month");
        int periodColumnIndex = headers.indexOf("Period");
        if (periodColumnIndex != -1) {
            validation.add(new ExcelBuilder.DropdownConfig(periodColumnIndex, period));
        }
        List<String> months = List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        int monthColumnIndex = headers.indexOf("Months");
        if (monthColumnIndex != -1) {
            validation.add(new ExcelBuilder.DropdownConfig(monthColumnIndex, months));
        }

        return validation;
    }

}
