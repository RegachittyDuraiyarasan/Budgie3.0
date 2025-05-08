package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollLoanDTO;
import com.hepl.budgie.entity.payroll.PayrollLoan;

import java.util.List;

public interface PayrollLoanService {
    List<PayrollLoan> fetch();

    String add(String save, PayrollLoanDTO request);

    String update();

    String status();

    String delete();
}
