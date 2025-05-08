package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PayrollArrearsEmpDTO {

    @NotEmpty(message = "{validation.error.notBeEmpty}")
    private List<String> empId;

    @NotNull(message = "{validation.error.notBlank}")
    @Past(message = "{validation.error.past}")
    private LocalDate withEffectDate;
}
