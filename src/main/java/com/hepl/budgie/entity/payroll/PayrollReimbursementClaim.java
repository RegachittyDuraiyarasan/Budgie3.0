package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_reimbursement_claims")
public class PayrollReimbursementClaim {
    @Id
    private String id;
    private String empId;
    private String payrollMonth;
    private ZonedDateTime endDate;
    private String finYear;
    private List<Reimbursement> reimbursement;
    private double reimbursementTotal;
    @Data
    public static class Reimbursement {
        private String fbpType;
        private List<ReimbursementBill> reimbursementBills;
        private double typeTotal;

    }
}
