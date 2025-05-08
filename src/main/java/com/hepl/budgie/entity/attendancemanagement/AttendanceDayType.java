package com.hepl.budgie.entity.attendancemanagement;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "attendance_day_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDayType {

    @Id
    private String id;
    private String dayTypeId;
    private String dayType;
    private String status = Status.ACTIVE.label;
}
