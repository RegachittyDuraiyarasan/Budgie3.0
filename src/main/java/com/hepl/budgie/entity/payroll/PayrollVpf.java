package com.hepl.budgie.entity.payroll;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_t_vpf_nps")
public class PayrollVpf {
    @Id
    private String id;
    private String rcpfId;
    private String type;
    private String empId;
    private String deductionType;
    private String amount;
    private String percentage;
    private String fromMonth;
    private String toMonth;
    private String hrEmpID;
    private LocalDate authorizedOn;
    private String status;

}