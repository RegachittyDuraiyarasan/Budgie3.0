package com.hepl.budgie.dto.probation;

import com.hepl.budgie.entity.probation.FollowUp;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class AddProbationDTO {
    private String empId;
    private String reportingManagerId;
    private Map<String,String> reportingManagerRatings;
    private Map<String,String> reportingManagerRemarks;
    private String status;
    private String extendedStatus;
    private String extendedMonths;
    private String finalRemarks;
    private String results;
    private String overAllRating;
    private List<FollowUp> followUps;
    private List<Map<String, Object>> mailTriggers;
    private String hrVerifyStatus;
    private String extendedHrVerifyStatus;
}
