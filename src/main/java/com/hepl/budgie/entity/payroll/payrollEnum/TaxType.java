package com.hepl.budgie.entity.payroll.payrollEnum;

import lombok.Getter;

@Getter
public enum TaxType {
    TAX("Tax"),
    SURCHARGE("Surcharge"),
    CESS("Cess");
    public final String label;

    private TaxType(String label) {
        this.label = label;
    }
}
