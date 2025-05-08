package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollPfDTO;
import com.hepl.budgie.dto.payroll.PayrollPfListDTO;
import java.util.List;
import com.hepl.budgie.entity.payroll.PayrollPf;

public interface PayrollPfService {
    void addorupdate(PayrollPfDTO request,String operation,String id);
    void delete(String id);
    List<PayrollPf> list();
    List<PayrollPf> listpf(String orgid);

    List<PayrollPfListDTO> listIndex();
}
