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
@Document(collection = "payroll_t_ctc_breakups")
public class PayrollCTCBreakups {
    @Id
    private String id;
    private String empId;
    private ZonedDateTime withEffectDate;
    private String financialYear;
    private String payrollMonth;
    private Map<String,Integer> earningColumns;
    private Map<String,Integer> deductionColumn;
    private int grossEarnings;
    private int grossDeductions;
    private int netPay;
    private int revisionOrder;
    private boolean delete;
    private String createdBy;
    private String updatedBy;
//    private ZonedDateTime createdAt;
//    private ZonedDateTime updatedAt;

}
