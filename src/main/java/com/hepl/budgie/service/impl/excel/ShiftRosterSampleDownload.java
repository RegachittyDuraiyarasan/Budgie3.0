package com.hepl.budgie.service.impl.excel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class ShiftRosterSampleDownload implements ExcelExport {

    private final JWTHelper jwtHelper;
    private final ShiftMasterRepository shiftMasterRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<HeaderList> prepareHeaders() {
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("Month", true, "String"),
                new HeaderList("Year", true, "String"),
                new HeaderList("1", false, "String"),
                new HeaderList("2", false, "String"),
                new HeaderList("3", false, "String"),
                new HeaderList("4", false, "String"),
                new HeaderList("5", false, "String"),
                new HeaderList("6", false, "String"),
                new HeaderList("7", false, "String"),
                new HeaderList("8", false, "String"),
                new HeaderList("9", false, "String"),
                new HeaderList("10", false, "String"),
                new HeaderList("11", false, "String"),
                new HeaderList("12", false, "String"),
                new HeaderList("13", false, "String"),
                new HeaderList("14", false, "String"),
                new HeaderList("15", false, "String"),
                new HeaderList("16", false, "String"),
                new HeaderList("17", false, "String"),
                new HeaderList("18", false, "String"),
                new HeaderList("19", false, "String"),
                new HeaderList("20", false, "String"),
                new HeaderList("21", false, "String"),
                new HeaderList("22", false, "String"),
                new HeaderList("23", false, "String"),
                new HeaderList("24", false, "String"),
                new HeaderList("25", false, "String"),
                new HeaderList("26", false, "String"),
                new HeaderList("27", false, "String"),
                new HeaderList("28", false, "String"),
                new HeaderList("29", false, "String"),
                new HeaderList("30", false, "String"),
                new HeaderList("31", false, "String")).toList());

        return headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns() {

        String orgId = jwtHelper.getOrganizationCode();
        List<String> headers = prepareHeaders().stream().map(HeaderList::getHeader).toList();
        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();

        List<String> shiftCodes = shiftMasterRepository.findAllShiftCodes(mongoTemplate, orgId);
        List<String> months = List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if ("month".equalsIgnoreCase(header)) {
                validation.add(new ExcelBuilder.DropdownConfig(i, months));
            }
            if (header.matches("\\d+") && Integer.parseInt(header) >= 1 && Integer.parseInt(header) <= 31) {
                validation.add(new ExcelBuilder.DropdownConfig(i, shiftCodes));
            }
        }
        return validation;
    }
}
