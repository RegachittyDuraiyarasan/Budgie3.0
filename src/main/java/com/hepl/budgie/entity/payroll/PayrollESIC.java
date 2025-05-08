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
@Document(collection = "payroll_m_esic")
public class PayrollESIC {
    @Id
    private  String id;
    private String esicId;
    private double employeeContribution;
    private double employerContribution;
    private String status;
    private String orgId;
}
