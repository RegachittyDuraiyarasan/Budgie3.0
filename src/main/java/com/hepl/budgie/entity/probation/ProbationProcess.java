package com.hepl.budgie.entity.probation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "probation_process")
public class ProbationProcess {
    @Id
    private String id;
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
    private String createdAT;
    private String createdBy;

}
