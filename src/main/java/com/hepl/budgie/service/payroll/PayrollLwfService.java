package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.entity.payroll.PayrollLwf;

import java.util.List;

public interface PayrollLwfService {
    boolean upsert(PayrollLwfDTO payrollLwfDTO,String operation);
    List<PayrollLwf> list();
    void delete(String id);
}
