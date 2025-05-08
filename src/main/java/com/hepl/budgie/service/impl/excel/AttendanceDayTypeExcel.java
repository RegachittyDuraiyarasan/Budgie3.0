package com.hepl.budgie.service.impl.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.repository.attendancemanagement.AttendanceDayTypeRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class AttendanceDayTypeExcel implements ExcelExport {

    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final AttendanceDayTypeRepository attendanceDayTypeRepository;
    private final ShiftMasterRepository shiftMasterRepository;

    @Override
    public List<HeaderList> prepareHeaders() {
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("Date", true, "Date"),
                new HeaderList("Day_Type", true, "String"),
                new HeaderList("Shift_Code", true, "String")).toList());

        return headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns() {

        String orgId = jwtHelper.getOrganizationCode();
        List<String> headers = prepareHeaders().stream().map(HeaderList::getHeader).toList();
        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();

        List<String> dayTypes = attendanceDayTypeRepository.findAllDayTypes(mongoTemplate, orgId);
        int dayTypeColumnIndex = new ArrayList<>(headers).indexOf("Day_Type");
        if (dayTypeColumnIndex != -1) {
            validation.add(new ExcelBuilder.DropdownConfig(dayTypeColumnIndex, dayTypes));
        }

        List<String> shiftCodes = shiftMasterRepository.findAllShiftCodes(mongoTemplate, orgId);
        int shiftCodeColumnIndex = new ArrayList<>(headers).indexOf("Shift_Code");
        if (shiftCodeColumnIndex != -1) {
            validation.add(new ExcelBuilder.DropdownConfig(shiftCodeColumnIndex, shiftCodes));
        }

        return validation;
    }
}
