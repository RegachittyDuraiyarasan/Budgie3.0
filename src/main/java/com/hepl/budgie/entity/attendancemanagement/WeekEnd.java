package com.hepl.budgie.entity.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekEnd {

    private String weekName;
    private String satDate;
    private String satStatus;
    private String sunDate;
    private String sunStatus;
}
