package com.hepl.budgie.dto.movement;

import lombok.Data;

@Data
public class ReviewerUpdateDTO {
    private String empId;
    private String officialReviewerStatus;
    private String assignedReviewerStatus;
    private String HRStatus;
    private String movementId;
    private String departmentNow;
    private String designationNow;
    private String supervisorNow;
    private String reviewerNow;

}
