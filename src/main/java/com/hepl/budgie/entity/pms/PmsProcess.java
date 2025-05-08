package com.hepl.budgie.entity.pms;

import lombok.Data;

import java.util.List;

@Data
public class PmsProcess {
    private String keyBusinessDriver;
    private List<String> keyResultArea;
    private List<String> measurementCriteria;
    private List<String> selfAssessmentRemark;
    private List<String> selfRating;
    private List<String> levelRemarks;
    private List<String> levelRatingValue;
}
