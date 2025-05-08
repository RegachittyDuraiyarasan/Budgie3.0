package com.hepl.budgie.dto.iiy;

import lombok.Data;


@Data
public class IIYEmployeeRequestDTO {
    private String department;
    private String empId;
    private String reportingManagerEmpId;
    private String divisionHeadId;
    private String fromDate;
    private String toDate;
    private String iiyDate;
    private String id;
    private String rmRemarks;
    private String rmStatus;
    private String courseCategory;
    private String financialYear;
    private String status;

}
