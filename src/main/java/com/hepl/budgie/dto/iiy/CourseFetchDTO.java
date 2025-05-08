package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CourseFetchDTO {
    private String id;
    private String courseName;
    private String category;
    private String allDepartment;
    private String allEmployee;
    private String[] department;
    private String[] employee;
    private String status;
}
