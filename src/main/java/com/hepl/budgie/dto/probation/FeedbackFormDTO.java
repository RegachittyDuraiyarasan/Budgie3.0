package com.hepl.budgie.dto.probation;

import lombok.Data;

import java.util.Map;
@Data
public class FeedbackFormDTO {
    private Map<String,String> reportingManagerRatings;
    private Map<String,String> reportingManagerRemarks;
    private String results;
    private String extendedMonths;
    private String finalRemarks;
    private String overAllRating;
    private String status;
    private String extendedStatus;
}
