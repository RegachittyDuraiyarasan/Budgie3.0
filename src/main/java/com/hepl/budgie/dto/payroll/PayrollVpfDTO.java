package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PayrollVpfDTO {

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "amount|percentage")
    private String deductionType;
    private String rcpfId;
    @NotBlank(message = "Employee is required")
    private String empId;
    private String amount;
    private String percentage;
    @NotBlank(message = "fromMonth is required")
    private String fromMonth;
    @NotBlank(message = "toMonth is required")
    private String toMonth;
    private String status;
    private String type;

    @AssertTrue(message = "Amount is required ")
    public boolean isAmountValid() {

        if (deductionType.equalsIgnoreCase("Amount")) {
            return amount != null && !amount.isBlank();
        }
        return true;
    }

    @AssertTrue(message = "Percentage is required ")
    public boolean isPercentageValid() {
        return !"percentage".equals(deductionType) || (percentage != null && !percentage.isBlank());
    }

}
