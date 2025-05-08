package com.hepl.budgie.service.excel;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationRule;

import java.util.List;
import java.util.Map;

public interface ExcelValidation {
    Map<String, List<ValidationRule>> getValidationRules(List<HeaderList> headerList);
}
