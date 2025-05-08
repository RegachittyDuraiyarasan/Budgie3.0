package com.hepl.budgie.service.impl.excelValidation;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationRule;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.excel.ExcelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
@Component
@RequiredArgsConstructor
public class CTCBreakupValidation implements ExcelValidation {
    private final MongoTemplate mongoTemplate;
    private final UserInfoRepository userInfoRepository;
    private final JWTHelper jwtHelper;
    @Override
    public Map<String, List<ValidationRule>> getValidationRules(List<HeaderList> headers) {
        Map<String, List<ValidationRule>> validationRules = new HashMap<>();

        for (HeaderList header : headers) {
            List<ValidationRule> rules = new ArrayList<>();

            if ("Employee_ID".equalsIgnoreCase(header.getHeader())) {
                rules.add(new ValidationRule(value -> value != null && !value.toString().trim().isEmpty(), "Employee ID is required."));
    //                rules.add(new ValidationRule(value -> value.toString().matches("^(\\d+[a-zA-Z0-9]+)$"), "Employee ID must be alphanumeric."));
                rules.add(new ValidationRule(value -> getActiveEmployees().contains(value.toString()), "Employee ID is not active."));
            } else if ("With_Effect_Date".equalsIgnoreCase(header.getHeader())) {
                rules.add(new ValidationRule(value -> value instanceof LocalDate, "With Effect Date must be a valid date."));
            } else if (header.getHeader().endsWith("(Annual)")) {
                rules.add(new ValidationRule(value -> value instanceof Integer && ((Integer) value) >= 0, header.getHeader() + " must be a positive number."));
            }

            validationRules.put(header.getHeader(), rules);
        }
        return validationRules;
    }
    private Set<String> getActiveEmployees(){
        return userInfoRepository.findActiveEmployeeIds(mongoTemplate, jwtHelper.getOrganizationCode());
    }
}
