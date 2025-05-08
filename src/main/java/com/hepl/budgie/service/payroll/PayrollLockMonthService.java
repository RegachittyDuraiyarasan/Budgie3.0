package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.AttendanceDateDTO;
import com.hepl.budgie.dto.payroll.AttendanceDateFetchDTO;
import com.hepl.budgie.dto.payroll.PayrollMonthDTO;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;

import java.util.List;

public interface PayrollLockMonthService {
    void attendanceDate(AttendanceDateDTO request);
    void generateMonth(String startDate);
    void updateLockMonth(String payrollMonth);

    AttendanceDateFetchDTO standardDate();

    List<PayrollMonthDTO> listPayrollMonth();
}
