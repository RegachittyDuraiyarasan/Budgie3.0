package com.hepl.budgie.dto.attendancemanagement;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDayTypeHistoryDTO {

    private String empId;
    private LocalDate date;
    private String dayType;
    private String shift;
    
}
