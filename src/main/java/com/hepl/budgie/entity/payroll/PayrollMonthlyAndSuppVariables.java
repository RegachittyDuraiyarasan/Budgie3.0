package com.hepl.budgie.entity.payroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("payroll_t_monthly_supplementary_variables")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollMonthlyAndSuppVariables {
    @Id
    private  String id;
    @NotBlank(message = "{validation.error.notBlank}")
    private String empId;
    private String variableType;
    private String payrollMonth;
    @NotEmpty(message = "{validation.error.notBlank}")
    private Map<String, Integer> componentValues;

}
