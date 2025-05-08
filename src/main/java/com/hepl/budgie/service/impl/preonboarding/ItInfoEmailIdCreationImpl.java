package com.hepl.budgie.service.impl.preonboarding;

import com.hepl.budgie.dto.preonboarding.ItInfoEmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;
import com.hepl.budgie.entity.preonboarding.EmailRequestDetails;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.preonboarding.ItInfoEmailIdCreationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItInfoEmailIdCreationImpl implements ItInfoEmailIdCreationService {

    private final UserInfoRepository userInfoRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;

    @Override
    public List<ItInfoEmailIdCreationDTO> fetch() {
        List<UserInfo> userInfoList = userInfoRepository.findByStatus("Active");
        List<ItInfoEmailIdCreationDTO> dtoList = new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {
            ItInfoEmailIdCreationDTO dto = new ItInfoEmailIdCreationDTO();
            dto.setEmployeeId(userInfo.getSections().getBasicDetails().getFirstName()+" - "+userInfo.getEmpId());
            dto.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
            dto.setEmail(userInfo.getSections().getContact().getPersonalEmailId());

            List<OnBoardingInfo> onboardingInfoList = onboardingInfoRepository
                    .findAllByEmailRequestDetailsIsEmailIdCreated(false);
            OnBoardingInfo matchingOnBoardingInfo = onboardingInfoList.stream()
                    .filter(info -> info.getEmpId().equals(userInfo.getEmpId()))
                    .findFirst()
                    .orElse(null);

            if (matchingOnBoardingInfo != null) {
                dto.setHrSuggestedEmail(matchingOnBoardingInfo.getEmailRequestDetails().getHrSuggestedMail());
            }

            dtoList.add(dto);
        }

        return dtoList;
    }


    @Override

    public void updateSuggestedEmails(List<UpdateEmailRequestDTO> requests) {

        for (UpdateEmailRequestDTO request : requests) {
            String empId = request.getEmpId();
            String suggestedMail = request.getSuggestedMail();
            if (StringUtils.isBlank(empId)) {
                log.warn(AppMessages.REQUEST_NULL_OR_EMPTY);
                continue;
            }

            if (StringUtils.isBlank(suggestedMail)) {
                log.warn(AppMessages.REQUEST_NULL_OR_EMPTY, empId);
                continue;
            }

            if (!suggestedMail.contains("@")) {
                log.warn(AppMessages.INVALID_MAIL_FORMAT, suggestedMail, empId);
                continue;
            }

            Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
            if (userInfoOpt.isPresent()) {
                UserInfo userInfo = userInfoOpt.get();
                Optional<OnBoardingInfo> onboardingInfoOpt = onboardingInfoRepository.findByEmpId(empId);

                OnBoardingInfo onboardingInfo = onboardingInfoOpt.orElseGet(() -> {
                    OnBoardingInfo newOnboardingInfo = new OnBoardingInfo();
                    newOnboardingInfo.setEmpId(empId);
                    return newOnboardingInfo;
                });

                // Ensure EmailRequestDetails is initialized
                if (onboardingInfo.getEmailRequestDetails() == null) {
                    onboardingInfo.setEmailRequestDetails(new EmailRequestDetails());
                }

                // Set email details
                EmailRequestDetails emailRequestDetails = onboardingInfo.getEmailRequestDetails();
                emailRequestDetails.setConfirmedMail(suggestedMail);
                emailRequestDetails.setIsEmailIdCreated(true);
                emailRequestDetails.setEmailIDCreatedBy("HEPL00001");
                emailRequestDetails.setEmailIdCreatedAt(LocalDateTime.now());
                userInfo.getSections().getWorkingInformation().setOfficialEmail(suggestedMail);
                userInfoRepository.save(userInfo);
                onboardingInfoRepository.save(onboardingInfo);
            } else {
                log.warn(AppMessages.EMPLOYEE_ID_NOT_FOUND_IN_USER_TABLE, empId);
            }
        }
    }

}
