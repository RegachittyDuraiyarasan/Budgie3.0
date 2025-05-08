package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_lock_current_month")
public class PayrollLockMonth {
    private String id;
    private String finYear;
    private String standardStartDate;
    private String standardEndDate;
    private String fromFinYear;
    private String toFinYear;
    private String attendanceLockDate;
    private String attendanceEmpLockDate;
    private String attendanceRepoLockDate;
    private List<PayrollMonths> payrollMonths;
    private String orgId;
    private String status = Status.ACTIVE.label;
    

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PayrollMonths {
        private Date startDate;
        private Date endDate;
        private String payrollMonth;
        private Boolean lockMonth;
        private Boolean payslip = false;
        private Boolean mail = false;
    }
}

