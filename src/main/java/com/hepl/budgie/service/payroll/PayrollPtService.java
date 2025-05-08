package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PTListDTO;
import com.hepl.budgie.dto.payroll.PayrollPtDTO;
import com.hepl.budgie.entity.payroll.PayrollPt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public interface PayrollPtService {
    List<PTListDTO> list();
    void add(PayrollPtDTO request);
}
