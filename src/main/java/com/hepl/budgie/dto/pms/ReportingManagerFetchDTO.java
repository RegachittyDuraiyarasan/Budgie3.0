package com.hepl.budgie.dto.pms;

import com.hepl.budgie.entity.pms.EmployeeFeedback;
import com.hepl.budgie.entity.pms.PmsProcess;
import lombok.Data;

import java.util.List;

@Data
public class ReportingManagerFetchDTO {
    private String empId;
    private String employeeName;
    private List<PmsProcess> pmsProcess;
    private String consolidatedSelfRating;
    private String pmsYear;
    private List<EmployeeFeedback> employeeFeedback;
    private String employeeSummary;
    private String overAllRating;
    private String reportingManagerRating;
}
