package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollFBPEmpIndexDTO {
    private List<PayrollFBPMaster> fbpComponent;
    private int specialAllowance;
    private boolean status = false;
}
