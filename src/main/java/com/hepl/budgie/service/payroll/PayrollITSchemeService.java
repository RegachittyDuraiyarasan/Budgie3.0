package com.hepl.budgie.service.payroll;

import java.util.*;

import com.hepl.budgie.dto.payroll.PayrollITSchemeDTO;
import com.hepl.budgie.dto.payroll.PayrollTypeDTO;
import com.hepl.budgie.entity.payroll.ITScheme;
import com.hepl.budgie.entity.payroll.PayrollITScheme;

public interface PayrollITSchemeService {

    PayrollITScheme savePayrollSection(String section, String description);

    PayrollITScheme savePayrollITScheme(PayrollITSchemeDTO scheme);

    List<PayrollTypeDTO> getPayrollType();

    List<ITScheme> getPayrollSchemes(String type);
    
}
