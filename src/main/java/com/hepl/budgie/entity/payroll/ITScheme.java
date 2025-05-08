package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ITScheme {

    private String title;
    private String shortName;
    private String slugName;
    private String schemeId;
    private long maxAmount;
    private long maxAbove60;
    private long max60to80;
    private long maxAbove80;
    private String status;
    
}
