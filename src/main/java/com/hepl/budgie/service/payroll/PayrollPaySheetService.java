package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollPaysheetDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface PayrollPaySheetService {
    Map<String, Object> getPayrollStatus();

    List<PayrollPaysheetDTO> runPaySheet(List<String> type) throws ExecutionException, InterruptedException;
}
