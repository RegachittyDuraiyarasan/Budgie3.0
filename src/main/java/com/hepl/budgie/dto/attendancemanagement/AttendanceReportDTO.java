package com.hepl.budgie.dto.attendancemanagement;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceReportDTO {

    private String empId;
    private String empName;
    private LocalDate date;
    private String inOut;
    private String punchInOrOut;    
}
