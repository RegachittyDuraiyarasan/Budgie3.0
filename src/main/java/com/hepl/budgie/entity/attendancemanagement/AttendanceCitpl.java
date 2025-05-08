package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDateTime;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "attendance_citpl")
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCitpl {

    @Id
    private String id;
    private String empId;
    private String punchIn;
    private String punchOut;
    private LocalDate attendanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
