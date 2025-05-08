package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatedDayType {

    private LocalDate date;
    private DayTypeDetail dayTypes;

}
