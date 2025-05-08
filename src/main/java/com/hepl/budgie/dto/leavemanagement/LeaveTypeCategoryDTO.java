package com.hepl.budgie.dto.leavemanagement;

import lombok.Data;

import java.util.List;

@Data
public class LeaveTypeCategoryDTO {
    private String id;
    private String name;
    private String code;
    private String description;
    private List<String> leaveScheme;
    private int periodicityDays;
    private String encashmentProcess;
    private String carryForward;
    private String status;
}




