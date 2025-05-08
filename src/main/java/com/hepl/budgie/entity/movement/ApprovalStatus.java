package com.hepl.budgie.entity.movement;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ApprovalStatus {
    private String status;
    private ZonedDateTime approvedAt;
    private String approvedBy;
}
