package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollFBPEmpListDTO {
    private String fbpId;
    private String fbpType;
    private int maxAmount;
    private int monthlyAmount;
    private int yearlyAmount;
    private String payrollMonth;
}
