package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.config.auditing.AuditAwareImpl;
import com.hepl.budgie.config.auditing.AuditInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_arrears")
public class PayrollArrears extends AuditInfo {
    @Id
    private String id;
    private String empId;
    private String empName;
    private ZonedDateTime withEffectDate;
    private String payrollMonth;
    private Map<String, Integer> arrearsValues;
    private int gross;
    private double arrDays;
    private String arrType;

}
