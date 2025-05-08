package com.hepl.budgie.service.impl.preonboarding;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.preonboarding.*;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.entity.preonboarding.*;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.master.ModuleMasterSettingsRepository;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.TemplateService;
import com.hepl.budgie.service.preonboarding.CandidateSeatingService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateSeatingServiceImpl implements CandidateSeatingService {

    private final UserInfoRepository userInfoRepository;
    private final UserExpEducationRepository userExpEducationRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;
    private final MasterSettingsRepository masterSettingsRepository;
    private final ModuleMasterSettingsRepository moduleMasterSettingsRepository;
    private final MongoTemplate mongoTemplate;
    private final TemplateService templateService;
    private final JWTHelper jwtHelper;

    @Override
    public List<SeatingRequestDTO> fetch() {
        List<UserInfo> userInfoList = userInfoRepository.findByStatus("Active");
        List<SeatingRequestDTO> seatingRequests = new ArrayList<>();

        for (UserInfo userInfo : userInfoList) {
            Optional<OnBoardingInfo> onboardingInfoOpt = onboardingInfoRepository.findByEmpId(userInfo.getEmpId());

            boolean seatingStatus = false;
            boolean idCardStatus = false;

            if (onboardingInfoOpt.isPresent()) {
                OnBoardingInfo onboardingInfo = onboardingInfoOpt.get();
                SeatingRequestDetails seatingRequestDetails = onboardingInfo.getSeatingRequestDetails();

                seatingStatus = seatingRequestDetails != null
                        && Boolean.TRUE.equals(seatingRequestDetails.getIsSeatingRequestInitiated());
                idCardStatus = seatingRequestDetails != null
                        && Boolean.TRUE.equals(seatingRequestDetails.getIsIdCardRequestInitiated());

                if (seatingStatus || idCardStatus) {
                    continue; // Skip fully processed records
                }
            }

            // Create DTO regardless of whether onboardingInfoOpt is present
            SeatingRequestDTO dto = new SeatingRequestDTO();
            dto.setEmpId(userInfo.getSections().getBasicDetails().getFirstName() + " - " + userInfo.getEmpId());
            dto.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
            dto.setEmail(userInfo.getSections().getContact().getPersonalEmailId());
            dto.setSeatingStatus(seatingStatus);
            dto.setIdCardStatus(idCardStatus);

            seatingRequests.add(dto);
        }
        return seatingRequests;
    }

    @Override
    public List<OnBoardingInfo> update(List<EmployeeUpdateRequestDto> updateRequests) {
        List<OnBoardingInfo> updatedRecords = new ArrayList<>();

        for (EmployeeUpdateRequestDto request : updateRequests) {
            String empId = request.getEmpId();
            Boolean isSeatingRequestInitiated = request.getIsSeatingRequestInitiated();
            Boolean isIdCardRequestInitiated = request.getIsIdCardRequestInitiated();

            // Proceed only if both requests are initiated
//            if (!Boolean.TRUE.equals(isSeatingRequestInitiated) || !Boolean.TRUE.equals(isIdCardRequestInitiated)) {
//                continue;
//            }

            userInfoRepository.findByEmpId(empId)
                    .filter(userInfo -> userInfo.getStatus().equals(Status.ACTIVE.label))
                    .ifPresent(userInfo -> {
                        OnBoardingInfo onBoardingInfo = onboardingInfoRepository.findByEmpId(empId)
                                .orElseGet(() -> {
                                    OnBoardingInfo newInfo = new OnBoardingInfo();
                                    newInfo.setEmpId(empId);
                                    return newInfo;
                                });
                        SeatingRequestDetails seatingRequestDetails = new SeatingRequestDetails();
                        seatingRequestDetails.setIsSeatingRequestInitiated(isSeatingRequestInitiated);
                        seatingRequestDetails.setSeatingRequestApprovedBy("HEPL0001");
                        seatingRequestDetails.setSeatingRequestApprovedAt(LocalDateTime.now());
                        seatingRequestDetails.setIsIdCardRequestInitiated(isIdCardRequestInitiated);
                        seatingRequestDetails.setIdCardApprovedBy("HEPL0002");
                        seatingRequestDetails.setIdCardApprovedAt(LocalDateTime.now());

                        onBoardingInfo.setSeatingRequestDetails(seatingRequestDetails);

                        onboardingInfoRepository.save(onBoardingInfo);
                        updatedRecords.add(onBoardingInfo);
                    });
        }

        return updatedRecords;
    }

    @Override
    public List<SeatingRequestDTO> fetchApprovedRequests() {
        List<UserInfo> userInfoList = userInfoRepository.findByStatus("Active");
        List<SeatingRequestDTO> approvedRequests = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            SeatingRequestDTO dto = new SeatingRequestDTO();
            dto.setEmpId(userInfo.getSections().getBasicDetails().getFirstName() + " - " + userInfo.getEmpId());
            dto.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
            dto.setEmail(userInfo.getSections().getContact().getPersonalEmailId());

            Optional<OnBoardingInfo> onboardingInfo = onboardingInfoRepository.findByEmpId(userInfo.getEmpId());

            boolean seatingStatus = onboardingInfo
                    .map(OnBoardingInfo::getSeatingRequestDetails)
                    .map(SeatingRequestDetails::getIsSeatingRequestInitiated)
                    .orElse(false);
            boolean idCardStatus = onboardingInfo
                    .map(OnBoardingInfo::getSeatingRequestDetails)
                    .map(SeatingRequestDetails::getIsIdCardRequestInitiated)
                    .orElse(false);

            if (seatingStatus || idCardStatus) {
                dto.setSeatingStatus(seatingStatus);
                dto.setIdCardStatus(idCardStatus);
                approvedRequests.add(dto);
            }
        }

        return approvedRequests;
    }

    @Override
    public List<OnBoardingInfo> insertPreOnboarding(String empId, List<CandidateSeatingDto> candidateSeatingDtoList) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        List<OnBoardingInfo> updatedRecords = new ArrayList<>();

        OnBoardingInfo onBoardingInfo = onboardingInfoRepository.findByEmpId(authenticatedEmpId)
                .orElseGet(() -> {
                    OnBoardingInfo newOnBoardingInfo = new OnBoardingInfo();
                    newOnBoardingInfo.setEmpId(authenticatedEmpId);
                    newOnBoardingInfo.setPreOnboardingProcess(new ArrayList<>());  // Ensure initialization
                    return newOnBoardingInfo;
                });

        if (onBoardingInfo.getPreOnboardingProcess() == null) {
            onBoardingInfo.setPreOnboardingProcess(new ArrayList<>());
        }

        List<PreOnboardingProcess> processList = onBoardingInfo.getPreOnboardingProcess();
        Set<String> existingTypes = processList.stream()
                .map(PreOnboardingProcess::getType)
                .collect(Collectors.toSet());

        boolean isUpdated = false;

        for (CandidateSeatingDto candidateSeatingDto : candidateSeatingDtoList) {
            if (!candidateSeatingDto.getVerified()) {
                continue;
            }

            String preOnboarding = candidateSeatingDto.getPreOnboarding();
            if (!existingTypes.contains(preOnboarding)) {
                PreOnboardingProcess preOnboardingProcess = new PreOnboardingProcess();
                preOnboardingProcess.setType(preOnboarding);
                preOnboardingProcess.setVerified(true);

                if (candidateSeatingDto.getDate() != null) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate localDate = LocalDate.parse(candidateSeatingDto.getDate(), formatter);
                        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.of("UTC"));
                        preOnboardingProcess.setDate(zonedDateTime);
                    } catch (DateTimeParseException e) {
                        log.error("Invalid date format: {}", candidateSeatingDto.getDate(), e);
                    }
                }

                processList.add(preOnboardingProcess);
                existingTypes.add(preOnboarding);
                isUpdated = true;
            }
        }

        if (isUpdated) {
            onBoardingInfo.setPreOnboardingProcess(processList);
            onboardingInfoRepository.save(onBoardingInfo);
            updatedRecords.add(onBoardingInfo);
        }

        return updatedRecords;
    }

    @Override
    public BuddyDetailsDTO fetchBuddyDetails(String empId) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();

        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(authenticatedEmpId);

        if (userInfoOptional.isEmpty() || userInfoOptional.get().getSections() == null) {
            return null;
        }
        String buddyId = userInfoOptional.get().getSections().getHrInformation().getBuddy().getManagerId();

        if (buddyId == null || buddyId.isEmpty()) {
            return null;
        }
        Optional<UserInfo> buddyInfoOptional = userInfoRepository.findByEmpId(buddyId);

        if (buddyInfoOptional.isEmpty() || buddyInfoOptional.get().getSections() == null) {
            return null;
        }
        UserInfo buddyInfo = buddyInfoOptional.get();
        BuddyDetailsDTO buddyDetailsDTO = new BuddyDetailsDTO();

        buddyDetailsDTO.setEmployeeId(buddyInfo.getEmpId());
        buddyDetailsDTO.setEmployeeName(
                buddyInfo.getSections().getBasicDetails().getFirstName() +
                        " " +
                        buddyInfo.getSections().getBasicDetails().getLastName());
        buddyDetailsDTO.setOfficialEmail(buddyInfo.getSections().getWorkingInformation().getOfficialEmail());
        buddyDetailsDTO.setMobileNumber(buddyInfo.getSections().getContact().getPrimaryContactNumber());
        return buddyDetailsDTO;
    }

    @Override
    public List<CandidateSeatingDto> getAll(String referenceName, String org, String empId) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        String org1 = jwtHelper.getOrganizationCode();
        Map<String, CandidateSeatingDto> dtoMap = new HashMap<>();

        Optional<OnBoardingInfo> onboardingInfoOptional = onboardingInfoRepository.findByEmpId(authenticatedEmpId);

        if (onboardingInfoOptional.isPresent()) {
            OnBoardingInfo onboardingInfo = onboardingInfoOptional.get();

            if (onboardingInfo.getPreOnboardingProcess() != null && !onboardingInfo.getPreOnboardingProcess().isEmpty()) {
                for (PreOnboardingProcess process : onboardingInfo.getPreOnboardingProcess()) {
                    String preOnboardingKey = process.getType().toLowerCase();

                    CandidateSeatingDto dto = new CandidateSeatingDto();
                    dto.setPreOnboarding(process.getType());
                    dto.setVerified(process.getVerified());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    dto.setDate(process.getDate() != null ? process.getDate().format(formatter) : null);

                    dtoMap.put(preOnboardingKey, dto);
                }
            }
        }

        Optional<MasterFormSettings> masterForm = masterSettingsRepository.fetchOptions(referenceName, org1, mongoTemplate);

        if (masterForm.isPresent()) {
            MasterFormSettings masterFormSettings = masterForm.get();

            if (masterFormSettings.getOptions() != null && !masterFormSettings.getOptions().isEmpty()) {
                for (MasterFormOptions option : masterFormSettings.getOptions()) {
                    String preOnboardingKey = option.getName().toLowerCase();

                    if (!dtoMap.containsKey(preOnboardingKey)) {
                        CandidateSeatingDto dto = new CandidateSeatingDto();
                        dto.setPreOnboarding(option.getName());
                        dto.setVerified(false);
                        dto.setDate(null);

                        dtoMap.put(preOnboardingKey, dto);
                    }
                }
            } else {
                log.warn(AppMessages.MASTER_SETTING_NOT_FOUND, referenceName, org);
            }
        } else {
            log.warn("No MasterFormSettings found for referenceName: {}, org: {}", referenceName, org);
        }

        return new ArrayList<>(dtoMap.values());
    }

    @Override
    public BuddyDTO fetchBuddy(String empId, String referenceName, String org) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        String org1 = jwtHelper.getOrganizationCode();

        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new IllegalArgumentException(AppMessages.USER_NOT_FOUND + authenticatedEmpId));

        BuddyDTO buddyDTO = new BuddyDTO();
        buddyDTO.setEmployeeId(userInfo.getEmpId());

        BasicDetails basicDetails = Optional.ofNullable(userInfo.getSections())
                .map(Sections::getBasicDetails)
                .orElseThrow(() -> new IllegalArgumentException("Basic details not found"));
        buddyDTO.setEmployeeName(basicDetails.getFirstName() + " " + basicDetails.getLastName());

        WorkingInformation workingInfo = Optional.ofNullable(userInfo.getSections())
                .map(Sections::getWorkingInformation)
                .orElse(new WorkingInformation());
        buddyDTO.setDesignation(workingInfo.getDesignation());
        buddyDTO.setWorkLocation(workingInfo.getWorkLocation());

        if (workingInfo.getDoj() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            buddyDTO.setDateOfJoining(workingInfo.getDoj().format(formatter));
        }

        Optional.ofNullable(userInfo.getSections())
                .map(Sections::getHrInformation)
                .map(HrInformation::getBuddy)
                .map(ReporteeDetail::getManagerId)
                .flatMap(userInfoRepository::findByEmpId)
                .map(user -> {
                    String firstName = Optional.ofNullable(user.getSections())
                            .map(Sections::getBasicDetails)
                            .map(BasicDetails::getFirstName)
                            .orElse("");

                    String empId1 = Optional.ofNullable(user.getEmpId()).orElse("");

                    return firstName + " - " + empId1;
                })
                .ifPresent(buddyDTO::setBuddyAssigned);


        Optional<OnBoardingInfo> onboardingInfoOptional = onboardingInfoRepository.findByEmpId(empId);
        OnBoardingInfo onBoardingInfo = onboardingInfoOptional.orElse(null);
        buddyDTO.setBuddyFeedBackStatus(onBoardingInfo != null && onBoardingInfo.isBuddyFeedBackStatus());

        List<FeedbackFieldsDTO> feedbackFieldsList;

        if (onboardingInfoOptional.isPresent() && onboardingInfoOptional.get().getBuddyFeedbackResponse() != null) {
            Map<String, BuddyFeedbackResponse> responseMap = onboardingInfoOptional.get()
                    .getBuddyFeedbackResponse().stream()
                    .filter(response -> response.getFieldName() != null)
                    .collect(Collectors.toMap(BuddyFeedbackResponse::getFieldName, response -> response));

            feedbackFieldsList = responseMap.values().stream()
                    .sorted(Comparator.comparing(BuddyFeedbackResponse::getFieldNo))
                    .map(response -> {
                        FeedbackFieldsDTO feedbackField = new FeedbackFieldsDTO();
                        feedbackField.setBuddyFeedbackFields(response.getFieldName());
                        feedbackField.setRemark(response.getRemark());

                        String selectedOption = response.getSelectedOption();
                        if (selectedOption != null) {
                            switch (selectedOption) {
                                case "Strongly Disagree":
                                    feedbackField.setStronglyDisagree(selectedOption);
                                    break;
                                case "Disagree":
                                    feedbackField.setDisagree(selectedOption);
                                    break;
                                case "Neither Agree Nor Disagree":
                                    feedbackField.setNeitherAgreeNorDisagree(selectedOption);
                                    break;
                                case "Agree":
                                    feedbackField.setAgree(selectedOption);
                                    break;
                                case "Strongly Agree":
                                    feedbackField.setStronglyAgree(selectedOption);
                                    break;
                            }
                        }
                        return feedbackField;
                    })
                    .collect(Collectors.toList());

        } else {
            ModuleMaster moduleMaster = moduleMasterSettingsRepository.findByReferenceName(referenceName, org1, mongoTemplate)
                    .orElseThrow(() -> new IllegalArgumentException(AppMessages.MASTER_FORM_SETTINGS));

            feedbackFieldsList = moduleMaster.getOptions().stream()
                    .filter(option -> option.containsKey("name"))
                    .map(option -> {
                        FeedbackFieldsDTO feedbackField = new FeedbackFieldsDTO();
                        feedbackField.setBuddyFeedbackFields((String) option.get("name"));

                        feedbackField.setAgrees(option.getOrDefault("agree", "").toString());
                        feedbackField.setRemarks(option.getOrDefault("remarks", "").toString());

                        return feedbackField;
                    }).collect(Collectors.toList());
        }

        buddyDTO.setFeedbackFields(feedbackFieldsList);
        return buddyDTO;
    }

    @Override
    public void updateBuddyFeedback(String empId, List<FeedbackFieldsDTO> feedbackFields, String referenceName, String org) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        String org1 = jwtHelper.getOrganizationCode();

        OnBoardingInfo onBoardingInfo = onboardingInfoRepository.findByEmpId(authenticatedEmpId)
                .orElseGet(() -> {
                    OnBoardingInfo newOnBoardingInfo = new OnBoardingInfo();
                    newOnBoardingInfo.setEmpId(authenticatedEmpId);
                    newOnBoardingInfo.setCreatedAt(LocalDateTime.now());
                    return newOnBoardingInfo;
                });

        ModuleMaster moduleMaster = moduleMasterSettingsRepository.findByReferenceName(referenceName, org1, mongoTemplate)
                .orElseThrow(() -> new IllegalArgumentException(AppMessages.MASTER_FORM_SETTINGS));

        Map<String, MasterFormOptions> masterOptionsMap = moduleMaster.getOptions()
                .stream()
                .map(this::convertToMasterFormOptions)
                .collect(Collectors.toMap(MasterFormOptions::getName, option -> option));

        List<BuddyFeedbackResponse> feedbackResponses = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);

        for (FeedbackFieldsDTO field : feedbackFields) {
            String question = field.getBuddyFeedbackFields();

            if (!masterOptionsMap.containsKey(question)) {
                throw new IllegalArgumentException(AppMessages.INVALID_FEEDBACK + question);
            }

            MasterFormOptions masterOption = masterOptionsMap.get(question);
            BuddyFeedbackResponse feedbackResponse = new BuddyFeedbackResponse();

            feedbackResponse.setFieldNo(counter.getAndIncrement());
            feedbackResponse.setFieldName(question);

            feedbackResponse.setRemark("true".equalsIgnoreCase(masterOption.getRemarks()) ? field.getRemark() : null);
            feedbackResponse.setRemarks("true".equalsIgnoreCase(masterOption.getRemarks()) ? "true" : "false");
            feedbackResponse.setAgrees("true".equalsIgnoreCase(masterOption.getAgree()) ? "true" : "false");

            if ("true".equalsIgnoreCase(masterOption.getAgree())) {
                if (field.getStronglyDisagree() != null) {
                    feedbackResponse.setSelectedOption("Strongly Disagree");
                } else if (field.getDisagree() != null) {
                    feedbackResponse.setSelectedOption("Disagree");
                } else if (field.getNeitherAgreeNorDisagree() != null) {
                    feedbackResponse.setSelectedOption("Neither Agree Nor Disagree");
                } else if (field.getAgree() != null) {
                    feedbackResponse.setSelectedOption("Agree");
                } else if (field.getStronglyAgree() != null) {
                    feedbackResponse.setSelectedOption("Strongly Agree");
                }
            }

            feedbackResponses.add(feedbackResponse);
        }
        onBoardingInfo.setBuddyFeedBackStatus(true);
        onBoardingInfo.setBuddyFeedbackResponse(feedbackResponses);
        onBoardingInfo.setUpdatedAt(LocalDateTime.now());

        onboardingInfoRepository.save(onBoardingInfo);
    }

    private MasterFormOptions convertToMasterFormOptions(Map<String, Object> optionMap) {
        MasterFormOptions masterFormOptions = new MasterFormOptions();

        masterFormOptions.setName(getStringValue(optionMap.get("name")));
        masterFormOptions.setValue(getStringValue(optionMap.get("value")));
        masterFormOptions.setAgree(getStringValue(optionMap.get("agree")));
        masterFormOptions.setRemarks(getStringValue(optionMap.get("remarks")));

        return masterFormOptions;
    }


    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : null;
    }


    @Override
    public List<InductionScheduleDTO> fetchInductionSchedule(String referenceName, String org) {
        List<InductionScheduleDTO> inductionScheduleDTOList = new ArrayList<>();
        Optional<ModuleMaster> masterFormOptional = moduleMasterSettingsRepository.findByReferenceName(referenceName,
                org, mongoTemplate);
        if (masterFormOptional.isPresent()) {
            ModuleMaster masterFormSettings = masterFormOptional.get();

            if (masterFormSettings.getOptions() != null && !masterFormSettings.getOptions().isEmpty()) {
                for (Map<String, Object> option : masterFormSettings.getOptions()) {
                    InductionScheduleDTO dto = new InductionScheduleDTO();
                    dto.setFrom((String) option.get("from"));
                    dto.setTo((String) option.get("to"));
                    dto.setProgram((String) option.get("program"));
                    dto.setTask((String) option.get("task"));
                    inductionScheduleDTOList.add(dto);
                }
            }
        }
        return inductionScheduleDTOList;
    }

    @Override
    public String fetchUserDetails(String empId) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();

        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(authenticatedEmpId);
        Optional<UserExpEducation> userExpEducationOptional = userExpEducationRepository.findByEmpId(authenticatedEmpId);
       Optional<OnBoardingInfo> onBoardingInfo = onboardingInfoRepository.findByEmpId(authenticatedEmpId);
        if (userInfoOptional.isPresent() && userExpEducationOptional.isPresent() && onBoardingInfo.isPresent()) {
            UserInfo userInfo = userInfoOptional.get();
            UserExpEducation userExpEducation = userExpEducationOptional.get();
            OnBoardingInfo info = onBoardingInfo.get();

            String firstName = userInfo.getSections().getBasicDetails().getFirstName();
            String designation = userInfo.getSections().getWorkingInformation().getDesignation();
            String department = userInfo.getSections().getWorkingInformation().getDepartment();
            ZonedDateTime doj = userInfo.getSections().getWorkingInformation().getDoj();
            String achievementEducation = info.getWelcomeAboard().getAchievementEducation();
            String achievementExperience = info.getWelcomeAboard().getAchievementExperience();
            String favPastime = info.getInterestingFacts().getFavPastime();
            String favHobbies = info.getInterestingFacts().getFavHobbies();
            String threePlaces = info.getInterestingFacts().getThreePlaces();
            String threeFood = info.getInterestingFacts().getThreeFood();
            String favSports = info.getInterestingFacts().getFavSports();
            String favMovie = info.getInterestingFacts().getFavMovie();
            String extracurricularActivities = info.getInterestingFacts().getExtracurricularActivities();
            String careerInspiration = info.getInterestingFacts().getCareerInspiration();
            String languageKnown = info.getInterestingFacts().getLanguageKnown();
            String interestingFact = info.getInterestingFacts().getInterestingFact();
            String myMotto = info.getInterestingFacts().getMyMotto();
            String favBook = info.getInterestingFacts().getFavBook();
            List<EducationDetails> educationDetailsList = userExpEducation.getEducationDetails();
            List<Map<String, Object>> educationDetails = new ArrayList<>();
            for (EducationDetails edu : educationDetailsList) {
            Map<String, Object> eduMap = new HashMap<>();
            eduMap.put("qualification", edu.getQualification());
            eduMap.put("institute", edu.getInstitute());
            eduMap.put("endOn", edu.getEndOn());
                educationDetails.add(eduMap);
            }
            List<ExperienceDetails> experienceDetailsList = userExpEducation.getExperienceDetails();
            List<Map<String, Object>> experienceDetails = new ArrayList<>();
            for (ExperienceDetails exp : experienceDetailsList) {
            Map<String, Object> expMap = new HashMap<>();
            expMap.put("jobTitle", exp.getJobTitle());
            expMap.put("company", exp.getCompanyName());
            expMap.put("year", exp.getEndOn());
                experienceDetails.add(expMap);
            }

            return templateService.getWelcomeOnboard(firstName, designation, department, doj, educationDetails,
                    experienceDetails,achievementEducation,achievementExperience,favPastime,favHobbies,threePlaces,threeFood,
                    favSports,favMovie,extracurricularActivities,careerInspiration,languageKnown,interestingFact,myMotto,favBook);
        } else {
            throw new UsernameNotFoundException(AppMessages.USER_NOT_FOUND + empId);
        }
    }

    @Override
    public String updateInterestingFacts(String empId, InterestingFactsDTO interestingFactsDTO) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        Optional<OnBoardingInfo> onBoardingInfoOptional = onboardingInfoRepository.findByEmpId(authenticatedEmpId);

        OnBoardingInfo onBoardingInfo = onBoardingInfoOptional.orElseGet(() -> {
            OnBoardingInfo newOnBoardingInfo = new OnBoardingInfo();
            newOnBoardingInfo.setEmpId(empId);

            return newOnBoardingInfo;
        });

        InterestingFacts interestingFacts = onBoardingInfo.getInterestingFacts() != null ? onBoardingInfo.getInterestingFacts() : new InterestingFacts();
        WelcomeAboard welcomeAboard = onBoardingInfo.getWelcomeAboard() != null ? onBoardingInfo.getWelcomeAboard() : new WelcomeAboard();
        interestingFacts.setFavPastime(interestingFactsDTO.getFavPastime());
        interestingFacts.setFavHobbies(interestingFactsDTO.getFavHobbies());
        interestingFacts.setThreePlaces(interestingFactsDTO.getThreePlaces());
        interestingFacts.setThreeFood(interestingFactsDTO.getThreeFood());
        interestingFacts.setFavSports(interestingFactsDTO.getFavSports());
        interestingFacts.setFavMovie(interestingFactsDTO.getFavMovie());
        interestingFacts.setExtracurricularActivities(interestingFactsDTO.getExtracurricularActivities());
        interestingFacts.setCareerInspiration(interestingFactsDTO.getCareerInspiration());
        interestingFacts.setLanguageKnown(interestingFactsDTO.getLanguageKnown());
        interestingFacts.setInterestingFact(interestingFactsDTO.getInterestingFact());
        interestingFacts.setMyMotto(interestingFactsDTO.getMyMotto());
        interestingFacts.setFavBook(interestingFactsDTO.getFavBook());

        welcomeAboard.setAchievementEducation(interestingFactsDTO.getAchievementsEducation());
        welcomeAboard.setAchievementExperience(interestingFactsDTO.getAchievementsExperience());
        onBoardingInfo.setInterestingFacts(interestingFacts);
        onBoardingInfo.setWelcomeAboard(welcomeAboard);
        onBoardingInfo.setWelcomeAboardStatus(true);
        onboardingInfoRepository.save(onBoardingInfo);
        return "Updated successfully";
    }

    @Override
    public WelcomeAboardDTO mapUserDetailsToDTO(UserExpEducation userExpEducation, UserInfo userInfo, OnBoardingInfo onBoardingInfo) {
        UserDTO userDTO = new UserDTO();
        if (userInfo != null && userInfo.getSections() != null) {
            userDTO.setUserName(userInfo.getSections().getBasicDetails().getFirstName());
            userDTO.setUserDesignation(userInfo.getSections().getWorkingInformation().getDesignation());
            userDTO.setUserDepartment(userInfo.getSections().getWorkingInformation().getDepartment());
            userDTO.setUserTodayDate(userInfo.getSections().getWorkingInformation().getDoj().toString());
        }
        List<EducationInterestingDTO> educationDTOs = new ArrayList<>();
        if (userExpEducation != null && userExpEducation.getEducationDetails() != null) {
            educationDTOs = userExpEducation.getEducationDetails().stream()
                    .filter(education -> "Active".equals(education.getStatus()))
                    .map(education -> new EducationInterestingDTO(
                            education.getQualification(),
                            education.getInstitute(),
                            education.getEndOn()))
                    .collect(Collectors.toList());
        }
        List<ExperienceInterestingDTO> experienceDTOs = new ArrayList<>();
        if (userExpEducation != null && userExpEducation.getExperienceDetails() != null) {
            experienceDTOs = userExpEducation.getExperienceDetails().stream()
                    .filter(experience -> "Active".equals(experience.getStatus()))
                    .map(experience -> new ExperienceInterestingDTO(
                            experience.getJobTitle(),
                            experience.getCompanyName(),
                            experience.getEndOn()))
                    .collect(Collectors.toList());
        }

        InterestingFactsDTO interestingFactsDTO = new InterestingFactsDTO();
        if (onBoardingInfo != null && onBoardingInfo.getInterestingFacts() != null) {
            InterestingFacts facts = onBoardingInfo.getInterestingFacts();
            interestingFactsDTO = new InterestingFactsDTO(
                    facts.getFavPastime(), facts.getFavHobbies(), facts.getThreePlaces(),
                    facts.getFavSports(), facts.getFavMovie(), facts.getExtracurricularActivities(),
                    facts.getCareerInspiration(), facts.getLanguageKnown(), facts.getInterestingFact(),
                    facts.getMyMotto(), facts.getFavBook(),facts.getThreeFood()
            );
        }
        Achievement achievement = new Achievement();
        if (onBoardingInfo != null && onBoardingInfo.getWelcomeAboard() != null) {
            achievement.setAchievementsEducation(onBoardingInfo.getWelcomeAboard().getAchievementEducation());
            achievement.setAchievementsExperience(onBoardingInfo.getWelcomeAboard().getAchievementExperience());
            achievement.setFavPastime(onBoardingInfo.getInterestingFacts().getFavPastime());
            achievement.setFavHobbies(onBoardingInfo.getInterestingFacts().getFavHobbies());
            achievement.setThreePlaces(onBoardingInfo.getInterestingFacts().getThreePlaces());
            achievement.setThreeFood(onBoardingInfo.getInterestingFacts().getThreeFood());
            achievement.setFavSports(onBoardingInfo.getInterestingFacts().getFavSports());
            achievement.setFavMovie(onBoardingInfo.getInterestingFacts().getFavMovie());
            achievement.setExtracurricularActivities(onBoardingInfo.getInterestingFacts().getExtracurricularActivities());
            achievement.setCareerInspiration(onBoardingInfo.getInterestingFacts().getCareerInspiration());
            achievement.setLanguageKnown(onBoardingInfo.getInterestingFacts().getLanguageKnown());
            achievement.setInterestingFact(onBoardingInfo.getInterestingFacts().getInterestingFact());
            achievement.setMyMotto(onBoardingInfo.getInterestingFacts().getMyMotto());
            achievement.setFavBook(onBoardingInfo.getInterestingFacts().getFavBook());

        } else {
            achievement.setAchievementsEducation(null);
            achievement.setAchievementsExperience(null);
            achievement.setFavPastime(null);
            achievement.setFavHobbies(null);
            achievement.setThreePlaces(null);
            achievement.setThreeFood(null);
            achievement.setFavSports(null);
            achievement.setFavMovie(null);
            achievement.setExtracurricularActivities(null);
            achievement.setCareerInspiration(null);
            achievement.setLanguageKnown(null);
            achievement.setInterestingFact(null);
            achievement.setMyMotto(null);
            achievement.setFavBook(null);
        }

        WelcomeAboardDTO welcomeAboardDTO = new WelcomeAboardDTO();
        welcomeAboardDTO.setUserDTO(userDTO);
        welcomeAboardDTO.setEducationDetails(educationDTOs);
        welcomeAboardDTO.setExperienceDetails(experienceDTOs);
        welcomeAboardDTO.setInterestingFactsDTO(interestingFactsDTO);
        welcomeAboardDTO.setAchievement(achievement);
        welcomeAboardDTO.setStatus(onBoardingInfo.isWelcomeAboardStatus());


        return welcomeAboardDTO;
    }

    @Override
    public List<InterestingFactsQuestionDTO> fetchInterestingFacts(String referenceName, String org) {
        List<InterestingFactsQuestionDTO> interestingFactsQuestionDTOS = new ArrayList<>();
        Optional<ModuleMaster> masterFormOptional = moduleMasterSettingsRepository.findByReferenceName(referenceName, org, mongoTemplate);
        if (masterFormOptional.isPresent()) {
            ModuleMaster masterFormSettings = masterFormOptional.get();
            if (masterFormSettings.getOptions() != null && !masterFormSettings.getOptions().isEmpty()) {
                for (Map<String, Object> option : masterFormSettings.getOptions()) {
                    InterestingFactsQuestionDTO dto = new InterestingFactsQuestionDTO();
                    dto.setName((String) option.get("name"));
                    dto.setLabel((String) option.get("name"));
                    interestingFactsQuestionDTOS.add(dto);
                }
            }
        }
        return interestingFactsQuestionDTOS;
    }
}
