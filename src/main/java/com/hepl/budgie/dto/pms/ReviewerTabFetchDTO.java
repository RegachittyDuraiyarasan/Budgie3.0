package com.hepl.budgie.dto.pms;

import com.hepl.budgie.entity.pms.EmployeeFeedback;
import com.hepl.budgie.entity.pms.PmsProcess;
import com.hepl.budgie.entity.workflow.EmployeeDetails;
import lombok.Data;

import java.util.List;

@Data
public class ReviewerTabFetchDTO {
    private String empId;
    private String empName;
    private List<PmsProcess> pmsProcess;
    private String consolidatedSelfRating;
    private String pmsYear;
    private List<EmployeeFeedback> employeeFeedback;
    private String employeeSummary;
    private String overAllRating;
    private String reportingManagerRating;
    private String reviewerRating;
    private String reportingManagerSummary;
    private String reviewerRecommendation;
    private String remarks;
    private int status;
    private int orgMailStatus;
    private int feedBackStatus;
}
