package com.hepl.budgie.service.impl.preonboarding;

import com.hepl.budgie.dto.preonboarding.EmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;
import com.hepl.budgie.entity.preonboarding.EmailRequestDetails;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.preonboarding.EmailIdCreationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailIdCreationServiceImpl implements EmailIdCreationService {
    private final UserInfoRepository userInfoRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;

    @Override
    public List<EmailIdCreationDTO> fetchByEmpIds() {
        List<UserInfo> allEmployees = userInfoRepository.findAll();
        List<OnBoardingInfo> allOnboardingInfo = onboardingInfoRepository.findAll();

        Map<String, OnBoardingInfo> onboardingMap = allOnboardingInfo.stream()
                .collect(Collectors.toMap(OnBoardingInfo::getEmpId, Function.identity()));

        List<EmailIdCreationDTO> emailList = new ArrayList<>();

        for (UserInfo user : allEmployees) {
            OnBoardingInfo onboardingInfo = onboardingMap.get(user.getEmpId());

            if (onboardingInfo != null
                    && onboardingInfo.getEmailRequestDetails() != null
                    && Boolean.TRUE.equals(onboardingInfo.getEmailRequestDetails().getIsEmailIdCreated())) {
                continue; 
            }

            EmailIdCreationDTO dto = new EmailIdCreationDTO();
            dto.setEmployeeId(user.getSections().getBasicDetails().getFirstName() + " - " + user.getEmpId());
            dto.setEmail(user.getSections().getContact().getPersonalEmailId());
            dto.setStatus(false);

            if (onboardingInfo != null && onboardingInfo.getEmailRequestDetails() != null) {
                String hrSuggestedMail = onboardingInfo.getEmailRequestDetails().getHrSuggestedMail();
                dto.setHrSuggestedEmail(hrSuggestedMail != null ? hrSuggestedMail : "null");
            } else {
                dto.setHrSuggestedEmail("null");
            }

            emailList.add(dto);
        }
        return emailList;
    }


    @Override
    public void updateSuggestedEmails(List<UpdateEmailRequestDTO> requests) {
        for (UpdateEmailRequestDTO request : requests) {
            String empId = request.getEmpId();
            String suggestedMail = request.getSuggestedMail();

            if (!isValidEmail(suggestedMail)) {
                log.warn(AppMessages.INVALID_MAIL_FORMAT, empId, suggestedMail);
                continue;
            }

            Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
            if (userInfoOpt.isEmpty()) {
                log.warn(AppMessages.EMPLOYEEID_NOT_FOUND, empId);
                continue;
            }

            Optional<OnBoardingInfo> onboardingInfoOpt = onboardingInfoRepository.findByEmpId(empId);

            OnBoardingInfo onboardingInfo = onboardingInfoOpt.orElseGet(() -> {
                log.info(AppMessages.NEW_ONBOARDING, empId);
                OnBoardingInfo newInfo = new OnBoardingInfo();
                newInfo.setEmpId(empId);
                return newInfo;
            });

            EmailRequestDetails emailRequestDetails = onboardingInfo.getEmailRequestDetails();
            if (emailRequestDetails == null) {
                emailRequestDetails = new EmailRequestDetails();
            }

            emailRequestDetails.setHrSuggestedMail(suggestedMail);
            emailRequestDetails.setIsEmailCreationInitiated(true);
            emailRequestDetails.setSuggestedBy("HEPL00001");
            emailRequestDetails.setEmailCreationInitiatedAt(LocalDateTime.now());

            onboardingInfo.setEmailRequestDetails(emailRequestDetails);
            onboardingInfoRepository.save(onboardingInfo);

            log.info(AppMessages.HR_SUGGESTEDMAIL, empId, suggestedMail);
        }
    }


    @Override
    public List<EmailIdCreationDTO> fetchAllEmpIds() {
        List<OnBoardingInfo> onboardingInfos = onboardingInfoRepository.findAllByEmailRequestDetailsIsEmailIdCreated(true);

        return onboardingInfos.stream()
                .filter(onboardingInfo -> userInfoRepository.findByEmpId(onboardingInfo.getEmpId()).isPresent())
                .map(onboardingInfo -> {
                    EmailRequestDetails emailRequestDetails = onboardingInfo.getEmailRequestDetails();

                    Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(onboardingInfo.getEmpId());
                    UserInfo userInfo = userInfoOpt.get();
                    EmailIdCreationDTO emailIdCreationDTO = new EmailIdCreationDTO();
                    emailIdCreationDTO.setEmployeeId(userInfo.getSections().getBasicDetails().getFirstName() + " - " + userInfo.getEmpId());
                    emailIdCreationDTO.setEmail(userInfo.getSections().getContact().getPersonalEmailId());
                    emailIdCreationDTO.setStatus(emailRequestDetails.getIsEmailIdCreated());
                    emailIdCreationDTO.setHrSuggestedEmail(emailRequestDetails.getConfirmedMail());

                    return emailIdCreationDTO;
                })
                .filter(dto -> dto.getHrSuggestedEmail() != null && !dto.getHrSuggestedEmail().trim().isEmpty())
                .toList();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

}

