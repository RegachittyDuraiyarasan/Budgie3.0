package com.hepl.budgie.service.impl.excelValidation;

import java.util.*;

public class ValidationErrorTransformer {
    public static List<Map<String, String>> transformValidationErrors(List<Map<String, Object>> data, String col){
        List<Map<String, String>> transformedList = new ArrayList<>();

        for (Map<String, Object> rowData : data) {
            String rowNumber = rowData.get("row").toString(); // Extract row number
            String colVal = rowData.get(col).toString(); // Extract Employee_ID

            Map<String, List<String>> validationErrors = (Map<String, List<String>>) rowData.get("ValidationErrors");

            validationErrors.forEach((column, messages) -> {
                if (!messages.isEmpty()) {
                    Map<String, String> transformedRow = new LinkedHashMap<>();
                    transformedRow.put("row", rowNumber);
                    transformedRow.put(col, colVal);
                    transformedRow.put("column", column);
                    transformedRow.put("message", String.join(", ", messages));
                    transformedList.add(transformedRow);
                }
            });
        }
        return transformedList;

    }
}
