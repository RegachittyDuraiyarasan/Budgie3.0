package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class PayrollMonthDTO {
    private Date startDate;
    private Date endDate;
    private String payrollMonth;
    private Boolean lockMonth;
    private Boolean payslip;
    private Boolean mail;
}
