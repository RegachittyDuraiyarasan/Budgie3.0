package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "attendance_muster")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceMuster {

    @Id
    private String id;
    private String empId;
    private String empName;
    private String designation;
    private String doj;
    private String workLocation;
    private List<DailyAttendance> attendanceInfo;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String monthYear;
    private String finYear;
    private double totalPresent;
    private double totalSick;
    private double totalCasualLeave;
    private double totalSickLeave;
    private double totalPrivilegeLeave;
    private double totalProbationaryLeave;
    private double totalLop;
    private double lopReversal;
    private double totalWeekOff;
    private double totalHolidays;
    private double totalLeave;
    private double totalDays;
    private double restDay;
    private double onDuty;
    private String remarks;

}
