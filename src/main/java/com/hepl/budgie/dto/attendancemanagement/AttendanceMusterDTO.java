package com.hepl.budgie.dto.attendancemanagement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceMusterDTO {

    private String employeeId;
    private String employeeName;
    private String designation;
    private String doj;
    private String location;
    private String month;
    private List<DateAttendance> attendanceData;
    private double totalNoOfPresentDays;
    private double totalLOP;
    private double weekOff;
    private double totalHolidays;
    private double totalDays;
    private double totalSickLeave;
    private double totalCasualLeave;
    private double totalPrivilegeLeave;
    private double totalProbationaryLeave;
    private double totalLeave;
    
}
