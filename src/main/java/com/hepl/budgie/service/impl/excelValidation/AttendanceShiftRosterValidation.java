package com.hepl.budgie.service.impl.excelValidation;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationRule;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.excel.ExcelValidation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceShiftRosterValidation implements ExcelValidation {

    private final ShiftMasterRepository shiftMasterRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final UserInfoRepository userInfoRepository;


    @Override
    public Map<String, List<ValidationRule>> getValidationRules(List<HeaderList> headers) {

        String orgId = jwtHelper.getOrganizationCode();
        Map<String, List<ValidationRule>> validationRules = new HashMap<>();

        List<String> months = List.of(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        List<String> shiftCodes = shiftMasterRepository.findAllShiftCodes(mongoTemplate, orgId);

        Set<String> headerNames = headers.stream()
                .map(HeaderList::getHeader)
                .collect(Collectors.toSet());

        for (HeaderList header : headers) {
            List<ValidationRule> rules = new ArrayList<>();
            String column = header.getHeader();

            if ("Employee_ID".equalsIgnoreCase(header.getHeader())) {
                rules.add(new ValidationRule(value -> value != null && !value.toString().trim().isEmpty(),
                        "Employee ID is required."));
                rules.add(new ValidationRule(value -> getActiveEmployees().contains(value.toString()),
                        "Employee ID must exist in the active employees list."));
            } else if ("Month".equalsIgnoreCase(column)) {
                rules.add(new ValidationRule(
                        value -> value != null && months.contains(value.toString()),
                        "Month must be one of: " + String.join(", ", months)));
            } else if ("Year".equalsIgnoreCase(column)) {
                rules.add(new ValidationRule(
                        value -> value == null || value.toString().matches("\\d{4}"),
                        "Year should be a 4-digit number (e.g., 2024)."));
            } else if (column.matches("^\\d{1,2}$")) {
                int day = Integer.parseInt(column);
                if (day >= 1 && day <= 31 && headerNames.contains(column)) {
                    rules.add(new ValidationRule(
                            value -> {
                                if (value == null || value.toString().trim().isEmpty())
                                    return true; // allow empty
                                String val = value.toString().trim();
                                return shiftCodes.stream().anyMatch(code -> code.equalsIgnoreCase(val));
                            },
                            "Shift must be one of: " + String.join(", ", shiftCodes)));

                }
            }
            validationRules.put(header.getHeader(), rules);
        }
        return validationRules;
    }

    private Set<String> getActiveEmployees() {
        return userInfoRepository.findActiveEmployeeIds(mongoTemplate, jwtHelper.getOrganizationCode());
    }

}
