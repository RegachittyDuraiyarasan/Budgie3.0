package com.hepl.budgie.entity.separation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hepl.budgie.config.annotation.ValidReviewerInfo;

import lombok.Data;
@Data
@ValidReviewerInfo(shortNoticeDays = "shortNoticeDays", waiver = "waiver")
public class ReviewerInfo {
    private String reviewerId;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private int shortNoticeDays;
    private String waiver;
    private LocalDate lastWorkingDay;
    private String status;
}
