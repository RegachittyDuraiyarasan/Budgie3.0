package com.hepl.budgie.dto.attendancemanagement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceWeekendDTO {

    private String month;

    private List<String> satDate;
    private List<String> satStatus;
    private List<String> sunDate;
    private List<String> sunStatus;
    
}
