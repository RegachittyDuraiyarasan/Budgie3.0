package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreviousEmploymentTaxDTO {

    private long incomeExemption;
    private long professionalTax;
    private long providentFund;
    private long taxableIncome;
    private long totalTax;
    
}
