package com.hepl.budgie.entity.payroll;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_lwf")
public class PayrollLwf {
    @Id
    private String id;
    private String lwfId;
    private String state;
    private String deductionType;
    private List<String> deductionMonth;
    private double employeeContribution;
    private double employerContribution;
    private double totalContribution;
    private String status;
    private String orgId;

}
