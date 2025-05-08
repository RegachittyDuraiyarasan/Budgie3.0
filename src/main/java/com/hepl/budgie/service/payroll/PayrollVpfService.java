package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollVpfDTO;
import com.hepl.budgie.entity.payroll.PayrollVpf;

import java.util.List;

public interface PayrollVpfService {
    void upsert(PayrollVpfDTO payrollVpfDTO,String operation);
    List<PayrollVpf> getAllData();
    String updateStatus(String id, String operation);



}
