package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ActivityFetchDTO {
    private String id;
    private String empId;
    private IIYEmployeeDetails employeeDetails;
    private String iiyDate;
    private String course;
    private String courseCategory;
    private String duration;
    private String remarks;
//    private String status;
    private String rmStatus;
    private String rmRemarks;
    private String description;
    private String certification;
    private String fileName;

}
