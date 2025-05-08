package com.hepl.budgie.dto.pms;

import com.hepl.budgie.entity.pms.PmsProcess;
import lombok.Data;

import java.util.List;

@Data
public class PmsDTO {
    private String action;
    private String empId;
    private String from;
    private String to;
    private String level;
    private List<PmsProcess> pmsProcess;
    private String finalRatingValue;
    private String consolidatedSelfRating;
    private String yearFrom;
    private String yearTo;
    private String pmsYear;
    private String finalRating;
    private String hierarchyLevel;
    private String actionLevel;
    private String recommendation;
    private String recommendationValue;
    private String reviewerSummary;
}
