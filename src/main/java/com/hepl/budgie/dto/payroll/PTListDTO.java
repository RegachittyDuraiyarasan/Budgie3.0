package com.hepl.budgie.dto.payroll;

import lombok.Data;

@Data
public class PTListDTO {
    private String ptId;
    private String state;
    private String periodicity;
    private String deductionType;
    private String startToEnd;
    private String deductionMonth;
    private String salaryFrom;
    private String salaryTo;
    private String taxAmount;
    private String gender;
}
