package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_tds_slab")
public class PayrollTds {
    @Id
    private String id;
    private String tdsSlabId;
    private String type;
    private String regime;
    private String ageLimit;
    private double salaryFrom;
    private double salaryTo;
    private double percentage;
    private double taxAmount;
    private String orgId;
    private String status;
}
