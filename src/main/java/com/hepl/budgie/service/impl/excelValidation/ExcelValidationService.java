package com.hepl.budgie.service.impl.excelValidation;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.dto.excel.ValidationRule;
import com.hepl.budgie.service.excel.ExcelValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExcelValidationService {
	private final Map<String, ExcelValidation> validationStrategies;

	public ExcelValidationService(List<ExcelValidation> strategies) {
		this.validationStrategies = strategies.stream()
				.collect(Collectors.toMap(strategy -> strategy.getClass().getSimpleName(), Function.identity()));
	}

	public ValidationResult validateExcelData(List<Map<String, Object>> excelData, List<HeaderList> headers,
			String importType) {

		log.info("Validation Functions - {}", validationStrategies);
		ExcelValidation strategy = validationStrategies.getOrDefault(importType, null);
		Map<String, List<ValidationRule>> validationRules = strategy.getValidationRules(headers);

		log.info("Validation Rule -{}", validationRules);

		List<Map<String, Object>> validRows = new ArrayList<>();
		List<Map<String, Object>> invalidRows = new ArrayList<>();

		for (Map<String, Object> row : excelData) {
			Map<String, List<String>> errors = validateRow(row, validationRules);
			log.info("data validation -{}", errors.entrySet());

			boolean hasErrors = errors.values().stream().anyMatch(list -> !list.isEmpty());

			if (!hasErrors) {
				validRows.add(row);
			} else {
				row.put("ValidationErrors", errors);
				invalidRows.add(row);
			}
		}

		return new ValidationResult(validRows, invalidRows);
	}

	private Map<String, List<String>> validateRow(Map<String, Object> rowData,
			Map<String, List<ValidationRule>> validationRules) {
		return rowData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
			String field = entry.getKey();
			Object value = entry.getValue();
			List<ValidationRule> rules = validationRules.getOrDefault(field, List.of());

			return rules.stream().filter(rule -> !rule.isValid(value, rowData)) 
					.map(ValidationRule::getErrorMessage).collect(Collectors.toList());
		}, (a, b) -> b));
	}
}
