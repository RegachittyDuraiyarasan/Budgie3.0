package com.hepl.budgie.entity.payroll.payrollEnum;

import lombok.Getter;

@Getter
public enum PayType {
    FIXED_PAY("Fixed Pay"), VARIABLE_PAY("Variable Pay");
    public final String label;

    private PayType(String label) {
        this.label = label;
    }

}
