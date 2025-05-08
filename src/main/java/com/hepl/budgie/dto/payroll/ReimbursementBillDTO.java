package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementBillDTO {
    private String empId;
    private String payrollMonth;
    private String finYear;
    private String reimbursementId;
    private String reimbursementType;
    private String billAmount;
    private String approvedBillAmount;
    private String billDate;
    private String billNo;
    private String claimDate;
    private String attachment;
    private String status;
    private String remarks;
}
