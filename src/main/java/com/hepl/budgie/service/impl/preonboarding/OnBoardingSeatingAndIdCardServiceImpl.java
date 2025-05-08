package com.hepl.budgie.service.impl.preonboarding;

import com.hepl.budgie.dto.preonboarding.SeatingRequestDTO;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.preonboarding.SeatingRequestDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.preonboarding.OnBoardingSeatingAndIdCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OnBoardingSeatingAndIdCardServiceImpl implements OnBoardingSeatingAndIdCardService {
    private final MongoTemplate mongoTemplate;
    private final OnboardingInfoRepository onboardingInfoRepository;
    private final UserInfoRepository userInfoRepository;

    @Override
    public List<SeatingRequestDTO> getByOnboardingStatus() { 
        List<SeatingRequestDTO> seatingRequestDTOS = new ArrayList<>();
        List<UserInfo> userInfos = userInfoRepository.findAll();
    
        if (userInfos != null) {
            for (UserInfo userInfo : userInfos) {
                SeatingRequestDTO dto = new SeatingRequestDTO();
                dto.setEmpId(userInfo.getEmpId());
                
                Optional<OnBoardingInfo> onBoardingInfoOpt = onboardingInfoRepository.findByEmpId(userInfo.getEmpId());
                boolean isSeatingRequest = false;
                boolean isIdCardRequest = false;
    
                if (onBoardingInfoOpt.isPresent()) {
                    OnBoardingInfo onBoardingInfo = onBoardingInfoOpt.get();
                    SeatingRequestDetails seatingRequestDetails = onBoardingInfo.getSeatingRequestDetails();
                    
                    if (seatingRequestDetails != null) {
                        isSeatingRequest = Boolean.TRUE.equals(seatingRequestDetails.getIsSeatingRequestInitiated());
                        isIdCardRequest = Boolean.TRUE.equals(seatingRequestDetails.getIsIdCardRequestInitiated());
                    }
                }
    
                // Only add users where both seatingStatus and idCardStatus are false
                if (!isSeatingRequest && !isIdCardRequest) {
                    if (userInfo.getSections() != null) {
                        if (userInfo.getSections().getBasicDetails() != null) {
                            dto.setEmpId(userInfo.getSections().getBasicDetails().getFirstName() + " - " + userInfo.getEmpId());
                        }
                        if (userInfo.getSections().getContact() != null) {
                            dto.setEmail(userInfo.getSections().getContact().getPersonalEmailId());
                            dto.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
                        }
                    }
                    dto.setSeatingStatus(false);
                    dto.setIdCardStatus(false);
    
                    seatingRequestDTOS.add(dto);
                }
            }
        }
    
        return seatingRequestDTOS;
    }
    
    @Override
    public List<SeatingRequestDTO> getByOnboardingStatusTrue() {
        List<SeatingRequestDTO> seatingRequestDTOS = new ArrayList<>();

        List<OnBoardingInfo> onboardingInfos = onboardingInfoRepository
                .findBySeatingRequestDetails_IsSeatingRequestInitiatedAndSeatingRequestDetails_IsIdCardRequestInitiated(
                        true, true);

        for (OnBoardingInfo onBoardingInfo : onboardingInfos) {
            String empId = onBoardingInfo.getEmpId();
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);

            if (userInfoOpt.isPresent()) {
                UserInfo userInfo = userInfoOpt.get();
                SeatingRequestDTO response = new SeatingRequestDTO();
                response.setEmpId(userInfo.getSections().getBasicDetails().getFirstName()+" - "+userInfo.getEmpId());
                response.setEmail(userInfo.getSections().getContact().getPersonalEmailId());
                response.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
                SeatingRequestDetails seatingRequestDetails = onBoardingInfo.getSeatingRequestDetails();
                response.setSeatingStatus(Boolean.TRUE.equals(seatingRequestDetails.getIsSeatingRequestInitiated()));
                response.setIdCardStatus(Boolean.TRUE.equals(seatingRequestDetails.getIsIdCardRequestInitiated()));
                seatingRequestDTOS.add(response);
            }
        }
        return seatingRequestDTOS;
    }

}
