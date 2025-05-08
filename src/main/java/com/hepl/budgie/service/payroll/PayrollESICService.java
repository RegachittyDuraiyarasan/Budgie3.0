package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollESICDto;
import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.entity.payroll.PayrollESIC;
import com.hepl.budgie.entity.payroll.PayrollLwf;

import java.util.List;

public interface PayrollESICService {
    boolean upsert(PayrollESICDto payrollESICDto, String operation);
    List<PayrollESIC> list();
    String updateStatus(String id, String operation);


}
