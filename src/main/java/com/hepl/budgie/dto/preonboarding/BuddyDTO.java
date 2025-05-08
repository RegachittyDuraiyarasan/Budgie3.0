package com.hepl.budgie.dto.preonboarding;

import lombok.Data;

import java.util.List;

@Data
public class BuddyDTO {
    private String employeeId;
    private String employeeName;
    private String designation;
    private String dateOfJoining;
    private String workLocation;
    private String buddyAssigned;
    private boolean buddyFeedBackStatus;
    private List<FeedbackFieldsDTO> feedbackFields;

}
