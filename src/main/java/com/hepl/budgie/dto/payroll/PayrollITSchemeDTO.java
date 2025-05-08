package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollITSchemeDTO {

    private String type;
    private String title;
    private String shortName;
    private long maxAmount;
    private long maxAbove60;
    private long max60to80;
    private long maxAbove80;
    
}
