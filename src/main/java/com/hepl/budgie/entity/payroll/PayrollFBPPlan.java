package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_fbp_plan")
public class PayrollFBPPlan {
    @Id
    private String id;
    private String fbpPlanId;
    private String empId;
    private String financialYear;
    private LocalDate endDate;
    private List<Fbp> fbp;
    private int totalAmount;
    private String status;
    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;

    @Data
    public static class Fbp {
        private String fbpType;
        private int maxAmount;
        private int monthlyAmount;
        private int yearlyAmount;
        private int fbpStatus;
        private String payrollMonth;
    }
}
