package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollLoanDTO {
    @NotBlank(message = "Employee ID cannot be empty")
    private String empId;
    @NotBlank(message = "Loan name cannot be empty")
    private String loanName;
    @NotBlank(message = "Loan type cannot be empty")
    private String loanType;
    @Positive(message = "Loan amount must be greater than zero")
    private int loanAmount;
    @Min(value = 1, message = "Number of installments must be at least 1")
    private int noOfInstallments;
    @NotNull(message = "Begin month cannot be null")
    private Date beginMonth;
}
