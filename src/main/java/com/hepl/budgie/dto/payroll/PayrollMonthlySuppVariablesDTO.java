package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;
@Data
public class PayrollMonthlySuppVariablesDTO {
    private String empId;
    private String empName;
    private String variableType;
    private String payrollMonth;
    private Map<String, Integer> componentValues;

}
