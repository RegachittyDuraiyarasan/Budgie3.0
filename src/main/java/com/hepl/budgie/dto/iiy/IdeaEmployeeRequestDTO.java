package com.hepl.budgie.dto.iiy;

import lombok.Data;

import java.util.Date;

@Data
public class IdeaEmployeeRequestDTO {
    private String department;
    private String empId;
    private String reportingManagerEmpId;
    private String fromDate;
    private String toDate;
    private String ideaDate;
    private String id;
    private String rmRemarks;
    private String rmWeightage;
    private String course;
    private int rmStatus;
    private String empName;
    private String reportingManagerName;
    private String reviewerId;
    private String reviewerName;
    private String divisionHeadId;
    private String divisionHeadName;
    private String designation;
    private String dateOfJoining;
    private String groupOfJoining;
    private String idea;
    private String category;
    private String weightage;
    private String description;
    private Date ideaDateValue;
    private String financialYear;
    private String IdeaId;


}
