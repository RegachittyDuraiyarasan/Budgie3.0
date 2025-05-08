package com.hepl.budgie.entity.payroll.object;

import lombok.Data;

@Data
public class EmployeeContribution {
    private String type;
    private Integer percentage;
    private Double fixedAmount;

    private String description;
}
