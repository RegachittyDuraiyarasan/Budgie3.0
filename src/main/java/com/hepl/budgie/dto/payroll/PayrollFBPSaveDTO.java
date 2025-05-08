package com.hepl.budgie.dto.payroll;

import lombok.Data;

import java.util.List;

@Data
public class PayrollFBPSaveDTO {
    private List<PayrollFBPEmpListDTO> fbpList;
    private String status;
}
