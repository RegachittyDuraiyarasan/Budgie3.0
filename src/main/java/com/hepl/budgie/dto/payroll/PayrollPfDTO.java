package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.object.EmployeeContribution;
import com.hepl.budgie.entity.payroll.object.EmployerContribution;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PayrollPfDTO {
    private String pfId;
    @NotBlank(message = "{validation.error.notBlank}")
    private String pfName;
    @NotBlank(message = "{validation.error.notBlank}")
    private String contributionType;
    private List<String> orgId;
    private Double percentage;
    private Double fixedAmount;
    private EmployerContribution employerContribution;
    private EmployeeContribution employeeContribution;
    private String component;
    private Double minimumSalary;
    private Double ceilingLimit;
    private Date effectiveDate;
    private String description;
    private Status status = Status.ACTIVE;

}
