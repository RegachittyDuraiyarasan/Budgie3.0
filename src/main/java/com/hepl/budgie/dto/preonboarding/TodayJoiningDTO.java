package com.hepl.budgie.dto.preonboarding;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TodayJoiningDTO {
    private boolean documentVerification;
    private boolean empIdGeneratedStatus;
    private String empId;
    private String email;
    private String mobileNumber;
    private boolean inductionMail;
    private boolean buddyMail;
    private boolean documentStatus;
    private boolean action;
    private boolean onboardingStatus;
    private boolean generationStatus;
    private String tempId;
}
