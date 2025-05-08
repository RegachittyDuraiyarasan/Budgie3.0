package com.hepl.budgie.entity.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyAttendance {

    private String date;
    private String attendance;

}
