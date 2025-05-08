package com.hepl.budgie.dto.attendancemanagement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceOverride {

    private String empId;
    private String monthYear;
    private List<AttendanceOverrideEntry> overrideList;
    
}
