package com.hepl.budgie.entity.payroll;

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
@Document(collection = "payroll_t_emp_loans")
public class PayrollLoan {
    @Id
    private String id;
    private String loanId;
    private String empId;
    private String loanName;
    private String loanType;
    private int loanAmount;
    private int noOfInstallments;
    private int emiAmount;
    private Date beginMonth;
    private int balanceAmount;
    private List<LoanDetails> installmentDetails;

    @Data
    public static class LoanDetails {
        private String payrollMonth;
        private int amount;
    }

}
