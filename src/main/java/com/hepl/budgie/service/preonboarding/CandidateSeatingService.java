package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.preonboarding.*;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.entity.userinfo.UserInfo;

import java.util.List;

public interface CandidateSeatingService {
    List<SeatingRequestDTO> fetch();
    List<OnBoardingInfo> update(List<EmployeeUpdateRequestDto> updateRequests);
    List<SeatingRequestDTO> fetchApprovedRequests();
    List<OnBoardingInfo> insertPreOnboarding (String empId,List <CandidateSeatingDto> candidateSeatingDto);
    BuddyDetailsDTO fetchBuddyDetails(String buddyId);
    List<CandidateSeatingDto> getAll(String referenceName, String org ,String empId);
    BuddyDTO fetchBuddy(String empId,String referenceName, String org);
    void updateBuddyFeedback(String empId, List<FeedbackFieldsDTO> buddyDTO,String referenceName,String org);
    List<InductionScheduleDTO> fetchInductionSchedule(String referenceName, String org);
    String fetchUserDetails(String empId);
    String updateInterestingFacts(String empId, InterestingFactsDTO interestingFactsDTO);
    WelcomeAboardDTO mapUserDetailsToDTO(UserExpEducation userExpEducation, UserInfo userInfo,OnBoardingInfo onBoardingInfo);
    List<InterestingFactsQuestionDTO> fetchInterestingFacts(String referenceName, String org);
}
