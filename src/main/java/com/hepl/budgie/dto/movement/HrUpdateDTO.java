package com.hepl.budgie.dto.movement;

import lombok.Data;

@Data
public class HrUpdateDTO {
    private String empId;
    private String officialReviewerStatus;
    private String assignedReviewerStatus;
    private String movementId;
    private String departmentNow;
    private String designationNow;
    private String supervisorNow;
    private String reviewerNow;
    private String hrStatus;
}
