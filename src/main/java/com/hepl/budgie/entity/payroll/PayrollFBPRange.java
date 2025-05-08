package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_fbp_range_details")
public class PayrollFBPRange {
    @Id
    private String id;
    private String rangeId;
    private int from;
    private int to;
    private String status= Status.ACTIVE.label;
    private String orgId;
}
