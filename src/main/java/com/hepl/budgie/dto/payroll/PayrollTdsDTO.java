package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.payroll.payrollEnum.TaxType;
import com.hepl.budgie.enums.AgeTypeEnum;
import com.hepl.budgie.enums.RegimeEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class PayrollTdsDTO {
    private String tdsSlabId;

    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = TaxType.class, message = "{validation.error.invalid}")
    private String type;

    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = RegimeEnum.class, message = "{validation.error.invalid}")
    private String regime;

    @ValueOfEnum(enumClass = AgeTypeEnum.class, message = "{validation.error.invalid}")
    private String ageLimit;

    @Positive(message = "{validation.error.notBlank}")
    private double percentage;

    private int salaryFrom;
    private int salaryTo;
    private int taxAmount;
    private String orgId;
    private String status;

    @AssertTrue(message = "{validation.error.notBlank}")
    private boolean isAgeLimit() {
        if (type != null && !type.isEmpty()) {
            if (type.equalsIgnoreCase(TaxType.TAX.label)) {
                return ageLimit != null && !ageLimit.isEmpty();
            }
        }
        return true;
    }
    @AssertTrue(message = "{validation.error.notBlank}")
    private boolean isSalaryFrom() {
        if (type != null && !type.isEmpty()) {
            if (type.equalsIgnoreCase(TaxType.TAX.label) || type.equalsIgnoreCase(TaxType.SURCHARGE.label)) {
                return salaryFrom >= 0;
            }
        }
        return true;
    }
    @AssertTrue(message = "{validation.error.notBlank}")
    private boolean isSalaryTo() {
        if (type != null && !type.isEmpty()) {
            if (type.equalsIgnoreCase(TaxType.TAX.label) || type.equalsIgnoreCase(TaxType.SURCHARGE.label)) {
                return salaryTo >= 0;
            }
        }
        return true;
    }
    @AssertTrue(message = "{validation.error.notBlank}")
    private boolean isTaxAmount() {
        if (type != null && !type.isEmpty()) {
            if (type.equalsIgnoreCase(TaxType.TAX.label)) {
                return taxAmount >= 0;
            }
        }
        return true;
    }
}