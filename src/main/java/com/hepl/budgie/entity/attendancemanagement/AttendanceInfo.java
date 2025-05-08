package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceInfo {

    private String attendanceDate;
    private String inTime;
    private String outTime;
    private String shift;
    private String totalWorkHours;
    private String actualWorkHours;
    private String shortFallHours;
    private String excessHours;
    private String attendanceData;
    private String attendanceSchema;
    private String regularization; 
    private String holiday;
    private String override;
    private String location;
    private String remark;
    private String updatedBy;
    private LocalDate updatedAt;
}
