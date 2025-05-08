package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceData {
    
    private String date;
    private List<String> punchIn;  
    private List<String> punchOut;
    private String updatedBy;
    private LocalDate updatedAt;
}
