package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class HRInfo {
    private String status;
    private String exitInterviewCompleted;
    private String settlementProcess;
    private String exitInterviewDiscussion;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;
}
