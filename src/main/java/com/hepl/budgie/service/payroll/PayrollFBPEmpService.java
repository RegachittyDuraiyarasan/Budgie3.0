package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollFBPEmpIndexDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPEmpListDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPSaveDTO;
import com.hepl.budgie.entity.payroll.PayrollFBPPlan;

import java.util.List;

public interface PayrollFBPEmpService {
    PayrollFBPEmpIndexDTO index();

    List<PayrollFBPEmpListDTO> fbpList();

    void fbpAdd(PayrollFBPSaveDTO request);
}
