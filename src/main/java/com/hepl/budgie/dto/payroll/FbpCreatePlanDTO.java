package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class FbpCreatePlanDTO {
    private String fbpPlanId;
    private String empId;
    private String empName;
    private Date endDate;
    private int consider;
}
