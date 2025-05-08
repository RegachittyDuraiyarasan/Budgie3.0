package com.hepl.budgie.service.impl.excelValidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hepl.budgie.config.security.JWTHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationRule;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;
import com.hepl.budgie.repository.leavemanagement.LeaveSchemeRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.excel.ExcelValidation;

@Component
@RequiredArgsConstructor
public class LeaveBalanceImportValidation implements ExcelValidation {

	private final MongoTemplate mongoTemplate;
	private final UserInfoRepository userInfoRepository;
	private final LeaveSchemeRepository leaveSchemeRepository;
	private final JWTHelper jwtHelper;
	private static final Set<String> VALID_MONTHS = Set.of("January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December");

	@Override
	public Map<String, List<ValidationRule>> getValidationRules(List<HeaderList> headers) {
		Map<String, List<ValidationRule>> validationRules = new HashMap<>();
		Map<String, String> employeeLeaveSchemes = userInfoRepository.fetchEmployeeLeaveSchemes(mongoTemplate);

		List<LeaveScheme> activeSchemes = leaveSchemeRepository.findByActiveStatus(jwtHelper.getOrganizationCode(),
				mongoTemplate);

		Set<String> yearlyLeaveSchemes = activeSchemes.stream()
				.filter(scheme -> "Yearly".equals(scheme.getPeriodicity())).map(LeaveScheme::getSchemeName)
				.collect(Collectors.toSet());

		Set<String> monthlyLeaveSchemes = activeSchemes.stream()
				.filter(scheme -> "Monthly".equals(scheme.getPeriodicity())).map(LeaveScheme::getSchemeName)
				.collect(Collectors.toSet());

		for (HeaderList header : headers) {
			List<ValidationRule> rules = new ArrayList<>();
			String headerName = header.getHeader();

			switch (headerName.toUpperCase()) {
			case "EMPLOYEE_ID":
				addEmployeeIdValidation(rules);
				break;
			case "PERIOD":
				addPeriodValidation(rules, employeeLeaveSchemes, yearlyLeaveSchemes, monthlyLeaveSchemes);
				break;
			case "YEAR":
				addYearValidation(rules);
				break;
			case "MONTHS":
				addMonthsValidation(rules);
				break;
			}

			if (!rules.isEmpty()) {
				validationRules.put(headerName, rules);
			}
		}

		return validationRules;
	}

	private void addEmployeeIdValidation(List<ValidationRule> rules) {
		rules.add(new ValidationRule(value -> value != null && !value.toString().trim().isEmpty(),
				"Employee ID is required."));
		rules.add(new ValidationRule(value -> value.toString().matches("^[a-zA-Z0-9]+$"),
				"Employee ID must be alphanumeric."));
		rules.add(new ValidationRule(value -> activeEmployees().contains(value.toString()),
				"Employee ID must exist in the active employees list."));
	}

	private void addPeriodValidation(List<ValidationRule> rules, Map<String, String> employeeLeaveSchemes,
			Set<String> yearlyLeaveSchemes, Set<String> monthlyLeaveSchemes) {
		rules.add(new ValidationRule(value -> value != null && !value.toString().trim().isEmpty(),
				"Period is required."));
		rules.add(new ValidationRule(
				value -> "Year".equalsIgnoreCase(value.toString()) || "Month".equalsIgnoreCase(value.toString()),
				"Period must be either 'Year' or 'Month'."));

		rules.add(new ValidationRule((value, context) -> {
			String employeeId = String.valueOf(context.get("Employee_ID"));
			if (employeeId != null && !employeeId.isEmpty()) {
				String leaveScheme = employeeLeaveSchemes.get(employeeId);
				if (leaveScheme != null) {
					if (yearlyLeaveSchemes.contains(leaveScheme)) {
						return "Year".equalsIgnoreCase(String.valueOf(value));
					} else if (monthlyLeaveSchemes.contains(leaveScheme)) {
						return "Month".equalsIgnoreCase(String.valueOf(value));
					}
				}
			}
			return true;
		}, "Period must be 'Year' for confirmed employees and 'Month' for probation/trainee employees"));
	}

	private void addYearValidation(List<ValidationRule> rules) {
		rules.add(new ValidationRule((value, context) -> {
			Object period = context.get("Period");
			if ("Year".equalsIgnoreCase(String.valueOf(period))) {
				return value != null && !String.valueOf(value).trim().isEmpty();
			}
			return true;
		}, "Year is required when Period is 'Year'."));

		rules.add(new ValidationRule((value, context) -> {
			Object period = context.get("Period");
			if ("Year".equalsIgnoreCase(String.valueOf(period)) && value != null) {
				try {
					int year = Integer.parseInt(value.toString());
					return year >= 2000 && year <= 2100;
				} catch (NumberFormatException e) {
					return false;
				}
			}
			return true;
		}, "Year must be a valid number between 2000 and 2100 when Period is 'Year'."));
	}

	private void addMonthsValidation(List<ValidationRule> rules) {
		rules.add(new ValidationRule((value, context) -> {
			Object period = context.get("Period");
			if ("Month".equalsIgnoreCase(String.valueOf(period))) {
				return value != null && !String.valueOf(value).trim().isEmpty();
			}
			return true;
		}, "Month is required when Period is 'Month'."));

		rules.add(new ValidationRule((value, context) -> {
			Object period = context.get("Period");
			if ("Month".equalsIgnoreCase(String.valueOf(period)) && value != null) {
				return VALID_MONTHS.contains(String.valueOf(value));
			}
			return true;
		}, "Month must be one of: " + String.join(", ", VALID_MONTHS)));
	}

	private Set<String> activeEmployees() {
		return userInfoRepository.findActiveEmployeeIds(mongoTemplate, jwtHelper.getOrganizationCode());
	}
}