package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviousEmploymentTax {

    private long incomeExemption;
    private long professionalTax;
    private long providentFund;
    private long taxableIncome;
    private long totalTax;
    private String status; 
    private String adminRemarks;
    private String employeeRemarks;

}
