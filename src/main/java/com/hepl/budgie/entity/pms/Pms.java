package com.hepl.budgie.entity.pms;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
@Data
public class Pms {
    @Id
    private String id;
    private String empId;
    private PmsEmployeeDetails pmsEmployeeDetails;
    private List<PmsProcess> pmsProcess;
    private List<EmployeeFeedback> employeeFeedBacks;
    private List<String> finalRating;
    private List<String> finalRatingValue;
    private String consolidatedSelfRating;
    private String status;
    private String pmsYear;
    private List<String> hierarchyLevel;
    private List<String> actionType;
    private List<String> recommendation;
    private List<String> recommendationValue;
    private List<PmsEmployeeFeedBack> employeeFeedback;
    private String overAllRating;
    private String employeeSummary;
    private String reviewerSummary;
    private int orgMailStatus;
}
