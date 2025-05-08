package com.hepl.budgie.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceOverrideEntry {

    private String date;
    private String session1;
    private String session2;

}
