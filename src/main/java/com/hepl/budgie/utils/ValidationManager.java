package com.hepl.budgie.utils;

import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.excel.ExcelValidation;
import com.hepl.budgie.service.impl.excelValidation.CTCBreakupValidation;
import com.hepl.budgie.service.impl.excelValidation.MonthlyVariablesValidation;

import java.util.Map;

public class ValidationManager {
    private final Map<String, ExcelValidation> strategies;

    public ValidationManager() {
        this.strategies = Map.of(
//                "CTC_VALIDATION", new CTCBreakupValidation()

        );
    }

    public ExcelValidation getStrategy(String importType) {
        return strategies.getOrDefault(importType, row -> Map.of()); // Default: No rules
    }

}
