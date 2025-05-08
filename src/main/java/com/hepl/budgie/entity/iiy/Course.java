package com.hepl.budgie.entity.iiy;

import com.hepl.budgie.config.auditing.AuditInfo;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "course")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends AuditInfo {
    @Id
    private String id;
    private String courseId;
    private String courseName;
    private String category;
    private String allDepartment;
    private String allEmployee;
    private String[] department;
    private String[] employee;
    private String status;

}
