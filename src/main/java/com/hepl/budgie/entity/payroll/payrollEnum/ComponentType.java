package com.hepl.budgie.entity.payroll.payrollEnum;

import lombok.Getter;

@Getter
public enum ComponentType {
    EARNINGS("Earnings"), DEDUCTION("Deduction"), REIMBURSEMENT("Reimbursement");
    public final String label;

    private ComponentType(String label) {
        this.label = label;
    }
}
