package com.hepl.budgie.entity.payroll;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollLetOutProperties {

    private long letableAmount;
    private long municipalTax;
    private long unrealizedTax;
    private long netValue;
    private long standardDeduction;
    private long loanInterest;
    private String lenderName;
    private String lenderPan;
    private long incomeOrLoss;
    private ZonedDateTime availableDate;
    private ZonedDateTime dateOfAcquisition;

}
