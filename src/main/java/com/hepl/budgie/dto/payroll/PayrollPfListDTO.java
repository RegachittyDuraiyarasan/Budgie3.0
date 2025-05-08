package com.hepl.budgie.dto.payroll;

import lombok.Data;

@Data
public class PayrollPfListDTO {
    private String empId;
    private String empName;
    private String pfLogic;
    private String pfName;
    private String payrollState;
}
