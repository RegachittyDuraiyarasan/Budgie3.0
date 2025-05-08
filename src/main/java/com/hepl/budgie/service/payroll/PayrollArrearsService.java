package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsListDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PayrollArrearsService {
    List<PayrollCTCBreakupsDTO> newJoinerArrears();
    List<List<String>> processExistingEmpArrears(List<String> employeeId, LocalDate withEffectDate);
    List<Map<String,Object>> arrearsList(String month);
}
