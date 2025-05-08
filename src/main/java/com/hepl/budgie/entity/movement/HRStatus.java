package com.hepl.budgie.entity.movement;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class HRStatus {
    private String status;
    private ZonedDateTime hrApprovedAt;
    private String hrApprovedBy;
}
