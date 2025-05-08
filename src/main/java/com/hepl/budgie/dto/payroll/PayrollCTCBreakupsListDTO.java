package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.entity.payroll.PayrollCTCBreakups;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PayrollCTCBreakupsListDTO {
    private String empId;
    private String empName;
    private ZonedDateTime doj;
    private List<PayrollCTCBreakups> ctcBreakups;

}
