package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class PayrollESICDto {
    private String esicId;
    @Positive(message = "{validation.error.notBlank}")
    private double employeeContribution;
    @Positive(message = "{validation.error.notBlank}")
    private double employerContribution;
    private String status;
    private String orgId;
}
