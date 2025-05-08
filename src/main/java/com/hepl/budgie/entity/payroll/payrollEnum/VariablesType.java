package com.hepl.budgie.entity.payroll.payrollEnum;

import lombok.Getter;

@Getter
public enum VariablesType {
    MONTHLY_VARIABLE("Monthly"),
    NEW_JOINER("New Joiner"),
    OLD_EMPLOYEE("Old Employee"),
    SUPP_VARIABLE("Supplementary");

    public final String label;

    private VariablesType(String label) {
        this.label = label;
    }
}
