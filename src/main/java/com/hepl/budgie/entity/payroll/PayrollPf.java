package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.object.EmployeeContribution;
import com.hepl.budgie.entity.payroll.object.EmployerContribution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_pf")
public class PayrollPf {
    @Id
    private String Id;
    private String pfId;
    private String pfName;
    private String contributionType;
    private Double percentage;
    private Double fixedAmount;
    private EmployerContribution employerContribution;
    private EmployeeContribution employeeContribution;
    private String component;
    private Double minimumSalary;
    private Double ceilingLimit;
    private Date effectiveDate;
    private String description;
    private List<String> orgId;
    private String status = Status.ACTIVE.label;


}
