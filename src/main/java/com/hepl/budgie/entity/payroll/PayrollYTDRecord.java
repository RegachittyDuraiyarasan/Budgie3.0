package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.config.auditing.AuditInfo;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_ytd_record")
public class PayrollYTDRecord extends AuditInfo {
    @Id
    private String id;
    private String empId;
    private String payrollMonth;
    private String financialYear;
    private Map<String, Integer> earnings;
    private Map<String, Integer> deductions;
    private Map<String, Integer> arrears;
    private Map<String, Integer> reimbursements;
    private List<Integer> basicTen;
    private List<Integer> basicFiftyForty;
    private List<Integer> hra;
    private int annualGrossIncome;
}
