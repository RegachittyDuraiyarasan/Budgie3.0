package com.hepl.budgie.service.payroll;

import com.hepl.budgie.entity.payroll.PayrollMonthlyAndSuppVariables;
import com.mongodb.bulk.BulkWriteResult;

import java.util.List;
import java.util.Map;

public interface PayrollMonthlyAndSuppVariableService {
    List<Map<String, Object>> list(String month, String variableType);
    BulkWriteResult excelImport(List<Map<String, Object>> validRows, String type);
    boolean singleUpload(PayrollMonthlyAndSuppVariables monthlySuppVariablesDTO);
    List<String> getHeaders(String variableType);
}
