package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IdeaFetchDTO {
    private String id;
    private String empId;
    private IIYEmployeeDetails employeeDetails;
    private String ideaDate;
    private String idea;
    private String course;
    private String category;
    private String weightage;
    private String description;
    private String rmStatus;
    private String rmWeightage;
    private String rmRemarks;

}
