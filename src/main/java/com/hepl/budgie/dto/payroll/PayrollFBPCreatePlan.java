package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollFBPCreatePlan {
    @NotBlank(message = "{validation.error.notBlank}")
    private String empId;
    private String empName;
    @NotNull(message = "{validation.error.notBeEmpty}")
    @FutureOrPresent(message = "{validation.error.futureOrPresent}")
    private LocalDate endDate;
}
