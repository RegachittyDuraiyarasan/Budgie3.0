package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "attendance_information")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceInformationCitpl {

    @Id
    private String id;
    private String empId;
    private String monthYear;
    private List<AttendanceInfo> attendanceInfo;

    public AttendanceInformationCitpl(String empId, String monthYear, List<AttendanceInfo> attendanceInfo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.empId = empId;
        this.monthYear = monthYear;
        this.attendanceInfo = attendanceInfo;
    }
    
}
