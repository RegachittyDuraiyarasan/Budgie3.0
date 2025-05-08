package com.hepl.budgie.dto.movement;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class FollowupDetails {
    private String department;
    private String designation;
    private String supervisor;
    private String reviewer;
    private ZonedDateTime hrApprovedAt;
}
