package com.hepl.budgie.entity.pms;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeTabFetchDTO {
    private String empId;
    private List<PmsProcess> pmsProcess;
    private String consolidatedSelfRating;
    private String pmsYear;
    private List<EmployeeFeedback> employeeFeedback;
    private String employeeSummary;
    private String overAllRating;
    private int orgMailStatus;
}
