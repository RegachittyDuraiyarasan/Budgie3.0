package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.config.auditing.AuditInfo;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_paysheet")
public class PayrollPaySheet extends AuditInfo {
    @Id
    private String id;
    private String empId;
    private String payrollMonth;
    private String stdDays;
    private String workDays;
    private Double lop;
    private Map<String, Integer> earnings;
    private Map<String, Integer> deductions;
    private Map<String, Integer> variables;
    private Map<String, Integer> arrears;
    private Map<String, Integer> reimbursements;
    private Integer grossEarnings;
    private Integer grossDeductions;
    private Integer netPay;
    private String status = DataOperations.PROCESSING.label;
}
