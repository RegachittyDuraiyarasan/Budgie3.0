package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.entity.payroll.*;
import com.hepl.budgie.entity.userinfo.PayrollDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class PayrollPaysheetDTO {
    private String empId;
    private ZonedDateTime dob;
    private String gender;
    private ZonedDateTime doj;
    private ZonedDateTime dol;
    private String payrollStatus;
    private PayrollDetails payrollDetails;
    private List<PayrollCTCBreakupsDTO> ctcBreakUps;
    private LopDTO attendanceMuster;
    private PayrollMonthlyAndSuppVariables monthly;
    private PayrollMonthlyAndSuppVariables supplementary;
    private PayrollArrears arrears;
    private PayrollVpf payrollVpf;
    private PayrollLoan payrollLoan;
    private PayrollPfDTO pfData;
}
