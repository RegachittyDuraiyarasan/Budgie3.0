package com.hepl.budgie.dto.excel;

import java.util.List;
import java.util.Map;

public class ValidationResult {
    private final List<Map<String, Object>> validRows;
    private final List<Map<String, Object>> invalidRows;

    public ValidationResult(List<Map<String, Object>> validRows, List<Map<String, Object>> invalidRows) {
        this.validRows = validRows;
        this.invalidRows = invalidRows;
    }

    public List<Map<String, Object>> getValidRows() {
        return validRows;
    }

    public List<Map<String, Object>> getInvalidRows() {
        return invalidRows;
    }

    public boolean hasErrors() {
        return !invalidRows.isEmpty();
    }
}
