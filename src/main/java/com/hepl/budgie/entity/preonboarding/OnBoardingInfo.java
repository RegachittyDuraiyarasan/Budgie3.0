package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_onboarding_info")
public class OnBoardingInfo {
    @Id
    private String id;
    private String empId;
    private String tempId;
    private String buddyId;
    private String empStatus;
    private boolean onboardingStatus;
    private String createdBy;
    private String updatedBy;
    private EmailRequestDetails emailRequestDetails;
    private SeatingRequestDetails seatingRequestDetails;
    private List<BuddyFeedbackResponse> buddyFeedbackResponse;
    private List<PreOnboardingProcess> preOnboardingProcess;
    private InterestingFacts interestingFacts;
    private WelcomeAboard welcomeAboard;
    private boolean welcomeAboardStatus;
    private DocumentVerification documentVerification;
    private boolean buddyFeedBackStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



