package com.hepl.budgie.entity.attendancemanagement;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("attendance_punch_information")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendancePunchInformation{

     @Id
    private String id;  
    private String empId; 
    private String monthYear;
    private List<AttendanceData> attendanceData;

}
