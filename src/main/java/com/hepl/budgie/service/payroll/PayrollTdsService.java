package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.dto.payroll.PayrollTdsDTO;
import com.hepl.budgie.entity.payroll.PayrollLwf;
import com.hepl.budgie.entity.payroll.PayrollTds;

import java.util.List;

public interface PayrollTdsService {
    boolean upsert(PayrollTdsDTO payrollLwfDTO, String operation);
    List<PayrollTds> list();
    void delete(String id);
    String updateStatus(String id);
}
