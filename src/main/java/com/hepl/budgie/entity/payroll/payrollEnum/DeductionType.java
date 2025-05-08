package com.hepl.budgie.entity.payroll.payrollEnum;

import lombok.Getter;

@Getter
public enum DeductionType {
    YEARLY("Yearly"), HALF_YEARLY("Half Yearly"), MONTHLY("Monthly");

    public final String label;

    private DeductionType(String label) {
        this.label = label;
    }
}
