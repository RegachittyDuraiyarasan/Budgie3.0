package com.hepl.budgie.utils;

import com.hepl.budgie.service.excel.ExcelExport;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SampleExcelFilesManger {
    private final Map<String, ExcelExport> strategies;

    public SampleExcelFilesManger(List<ExcelExport> excelExportList) {
        this.strategies = excelExportList.stream()
                .collect(Collectors.toMap(path -> path.getClass().getSimpleName(), Function.identity()));
    }

    public ExcelExport getSampleExcelFile(String importType) {
        return strategies.getOrDefault(importType, null); // Default: No rules
    }
}
