package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.payroll.payrollEnum.DeductionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PayrollLwfDTO {
    private String lwfId;
    @NotBlank(message = "{validation.error.notBlank}")
    private String state;
    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = DeductionType.class, message = "{validation.error.invalid}")
    private String deductionType;
    @NotEmpty(message = "{validation.error.notBeEmpty}")
    private List<String> deductionMonth;
    @Positive(message = "{validation.error.notBlank}")
    private double employeeContribution;
    @Positive(message = "{validation.error.notBlank}")
    private double employerContribution;
    private double totalContribution;
    private String orgId;
    private String status;

    @AssertTrue(message = "Invalid deduction month configuration")
    private boolean isDeductionMonth() {
        if (deductionType != null && !deductionType.isEmpty()) {
            if (deductionType.equalsIgnoreCase(DeductionType.YEARLY.label)) {
                return deductionMonth.size() == 1;
            } else if (deductionType.equalsIgnoreCase(DeductionType.HALF_YEARLY.label)) {
                return deductionMonth.size() == 2;
            } else if (deductionType.equalsIgnoreCase(DeductionType.MONTHLY.label)) {
                return deductionMonth.size() == 12;
            }
        }
        return true;
    }
}
