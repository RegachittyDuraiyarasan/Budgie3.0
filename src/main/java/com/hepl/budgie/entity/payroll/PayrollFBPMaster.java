package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_fbp_master")
public class PayrollFBPMaster {
    @Id
    private String id;
    private String rangeId;
    private String fbpId;
    private String fbpType;
    private String payType;
    private int amount;
    private String status= Status.ACTIVE.label;
    private String orgId;
}
