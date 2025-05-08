package com.hepl.budgie.service.impl.pms;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.pms.PmsDTO;
import com.hepl.budgie.dto.pms.ReportingManagerFetchDTO;
import com.hepl.budgie.dto.pms.ReviewerTabFetchDTO;
import com.hepl.budgie.entity.pms.*;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.enums.PmsLevelAndAction;
import com.hepl.budgie.mapper.pms.PmsMapper;
import com.hepl.budgie.repository.master.ModuleMasterSettingsRepository;
import com.hepl.budgie.repository.pms.PmsLevelMasterRepository;
import com.hepl.budgie.repository.pms.PmsModifyRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.pms.PmsLevelMasterService;
import com.hepl.budgie.service.pms.PmsModifyService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.UserDetailsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PmsModifyServiceImplementation implements PmsModifyService {
    private final JWTHelper jwtHelper;
    private final PmsModifyRepository pmsModifyRepository;
    private final PmsLevelMasterService pmsLevelMasterService;
    private final MongoTemplate mongoTemplate;
    private final PmsMapper pmsMapper;
    private final PmsLevelMasterRepository pmsLevelMasterRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserDetailsUtil userDetails;
    private final ModuleMasterSettingsRepository moduleMasterSettingsRepository;

    @Override
    public boolean addPms(PmsDTO request) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();

        // Validate Business Key Drivers
        List<String> businessKeyDrivers = request.getPmsProcess().stream()
                .map(PmsProcess::getKeyBusinessDriver)
                .toList();

        if (!new HashSet<>(businessKeyDrivers).containsAll(Arrays.asList(PmsLevelAndAction.KEY_CUSTOMER.getLabel(), PmsLevelAndAction.KEY_PROCESS.getLabel(), PmsLevelAndAction.KEY_PEOPLE.getLabel()))) {
            throw new CustomResponseStatusException(AppMessages.KEY_DRIVERS_NOT_MATCH, HttpStatus.CONFLICT, null);
        }
        UserInfo userInfo = userInfoRepository.findByEmpId(authenticatedEmpId)
                .orElseThrow(() -> new CustomResponseStatusException(AppMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND, null));

        PmsEmployeeDetails pmsEmployeeDetails = PmsMapper.INSTANCE.toPmsEmployeeDetails(userInfo, userInfoRepository);

        String levelName = PmsLevelAndAction.EMPLOYEE.getLabel();
        String status = pmsLevelMasterService.fetchStatusPmsRequest(levelName, request.getAction());
        String actionCount = pmsLevelMasterService.fetchCountPmsRequest(levelName, request.getAction());

        // Check if data exists
        Pms existingData = pmsModifyRepository.findByEmpIdAndPmsYear(authenticatedEmpId, request.getPmsYear(), jwtHelper.getOrganizationCode(), mongoTemplate);

        if (existingData != null) {
            if (!existingData.getPmsYear().equals(request.getPmsYear())) {
                throw new CustomResponseStatusException(AppMessages.PMS_YEAR_NOT_FOUND, HttpStatus.NOT_FOUND, null);
            }

            if ((PmsLevelAndAction.ACTION_MULTIPLE.getLabel().equals(actionCount) && existingData.getStatus().equals(status)) ||
                    (PmsLevelAndAction.ACTION_SINGLE.getLabel().equals(actionCount) && !existingData.getStatus().equals(status))) {
                existingData.setStatus(status);
                existingData.setPmsProcess(request.getPmsProcess());
                existingData.setConsolidatedSelfRating(request.getConsolidatedSelfRating());
                existingData.setHierarchyLevel(Collections.singletonList(levelName));
                existingData.setActionType(Collections.singletonList(request.getAction()));
                existingData.setPmsYear(request.getPmsYear());

                pmsModifyRepository.saveOrUpdate(existingData, jwtHelper.getOrganizationCode(), mongoTemplate);
                return false;
            }
            return true; // Indicating data is already submitted
        } else {
            // Insert new record
            Pms pms = pmsMapper.toEntity(request);
            pms.setEmpId(authenticatedEmpId);
            pms.setStatus(status);
            pms.setHierarchyLevel(Collections.singletonList(levelName));
            pms.setActionType(Collections.singletonList(request.getAction()));
            pms.setOrgMailStatus(0);
            pms.setPmsEmployeeDetails(pmsEmployeeDetails);

            pmsModifyRepository.saveOrUpdate(pms, jwtHelper.getOrganizationCode(), mongoTemplate);
            return false; // Indicating insert happened
        }
    }

    @Override
    public Object fetchDataByLevel(PmsListDTO request) {
        String level = request.getLevel();
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        String organization = jwtHelper.getOrganizationCode();
        String primaryReportingManager = request.getPrimaryReportingManager();
        List<String> status = pmsLevelMasterService.fetchFlowPmsRequest(level, organization, mongoTemplate);

        if (PmsLevelAndAction.EMPLOYEE.getLabel().equals(level)) {
            List<Pms> pmsRequests = pmsModifyRepository.findDataByLevel(request, status, authenticatedEmpId, organization, mongoTemplate);
            return pmsMapper.toEmployeeTabFetchDTOList(pmsRequests);
        }
        else if (PmsLevelAndAction.REPORTING_MANAGER.getLabel().equals(level)) {
            List<Pms> pmsRequests = pmsModifyRepository.findDataByReportingManager(
                    request, authenticatedEmpId, status, mongoTemplate, organization);

            // Convert the entire list at once
            List<ReportingManagerFetchDTO> pmsDTOs = pmsMapper.toReportingManagerTabFetchDTOList(pmsRequests);

            // Update each DTO with ratings
            for (int i = 0; i < pmsRequests.size(); i++) {
                Pms pms = pmsRequests.get(i);
                ReportingManagerFetchDTO dto = pmsDTOs.get(i);
                dto.setReportingManagerRating(fetchFinalRatingValueByLevel(PmsLevelAndAction.REPORTING_MANAGER.getLabel(), pms.getFinalRating(), pms.getFinalRatingValue()));
            }

            return pmsDTOs;
        }
        else if (PmsLevelAndAction.REVIEWER.getLabel().equals(level)) {
            List<Pms> pmsRequests = pmsModifyRepository.findDataByReviewer(
                    request, authenticatedEmpId, primaryReportingManager, mongoTemplate, organization);

            // Convert the entire list at once
            List<ReviewerTabFetchDTO> pmsDTOs = pmsMapper.toReviewerTabFetchDTOList(pmsRequests);

            // Update each DTO with required details
            for (int i = 0; i < pmsRequests.size(); i++) {
                Pms pms = pmsRequests.get(i);
                ReviewerTabFetchDTO dto = pmsDTOs.get(i);
                dto.setEmpName(userDetails.getUserName(dto.getEmpId()));


                if (pms.getFinalRating() != null && pms.getFinalRatingValue() != null) {
                    dto.setReportingManagerRating(fetchFinalRatingValueByLevel("Reporting Manager",
                            pms.getFinalRating(), pms.getFinalRatingValue()));
                    dto.setReviewerRating(fetchFinalRatingValueByLevel(PmsLevelAndAction.REVIEWER.getLabel(),
                            pms.getFinalRating(), pms.getFinalRatingValue()));
                }
                if (pms.getRecommendation() != null && pms.getRecommendationValue() != null) {
                    dto.setReviewerRecommendation(fetchRecommendationByLevel(PmsLevelAndAction.REVIEWER.getLabel(),
                            pms.getRecommendation(), pms.getRecommendationValue()));
                }
                if (pms.getOrgMailStatus() == 1) {
                    dto.setOverAllRating(pms.getOverAllRating());
                } else {
                    dto.setOverAllRating("");
                }
            }

            return pmsDTOs;
        }

        return Collections.emptyList();
    }

    @Override
    public void updatePmsByLevel(List<PmsDTO> updatedPmsDataList) {
        List<PmsLevel> activeStatuses = pmsLevelMasterRepository.findByActiveStatus(1, mongoTemplate, jwtHelper.getOrganizationCode());
        List<String> Ratings = activeStatuses.stream()
                .map(PmsLevel::getLevelName)
                .filter(levelName -> !PmsLevelAndAction.EMPLOYEE.getLabel().equals(levelName))
                .toList();

        for (PmsDTO updatedPmsData : updatedPmsDataList) {
            if (PmsLevelAndAction.REPORTING_MANAGER.getLabel().equalsIgnoreCase(updatedPmsData.getLevel())) {
                String managerId = userInfoRepository.findManagerIdByEmpId(updatedPmsData.getEmpId(), mongoTemplate);
                if (managerId == null || !managerId.equals(jwtHelper.getUserRefDetail().getEmpId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,AppMessages.PMS_RM_UNAUTHORIZED);
                }
            }
            String levelName = updatedPmsData.getLevel();
            String status = pmsLevelMasterService.fetchStatusPmsRequest(levelName, updatedPmsData.getAction());
            Pms pms = pmsModifyRepository.findByEmpIdAndPmsYear(
                    updatedPmsData.getEmpId(),
                    updatedPmsData.getPmsYear(), jwtHelper.getOrganizationCode(), mongoTemplate);

            if (pms == null) {
                throw new CustomResponseStatusException(AppMessages.PMS_DATA_NOT_FOUND, HttpStatus.NOT_FOUND,null);
            }

            String actionCount = pmsLevelMasterService.fetchCountPmsRequest(levelName, updatedPmsData.getAction());

            Map<String, String> hierarchyArray = keyValueMap(pms.getHierarchyLevel(), pms.getActionType());
            if ("Single".equals(actionCount) && hierarchyArray.containsKey(levelName)) {
                if (hierarchyArray.get(levelName).equals(updatedPmsData.getAction())) {
                    throw new RuntimeException("Already Submitted");
                }
            }

            List<String> hierarchyLevel = updateHierarchyLevel(pms.getHierarchyLevel(), pms.getActionType(), levelName, updatedPmsData.getAction());
            List<String> actionType = updateActionType(pms.getActionType(), hierarchyLevel, levelName, updatedPmsData.getAction());
            List<String> finalRating = new ArrayList<>(Ratings);
            List<String> finalRatingValue = updateFinalRatingValue(Ratings, pms.getFinalRatingValue(), updatedPmsData.getFinalRatingValue());
            List<PmsProcess> pmsProcessToUpdate = Collections.emptyList();
            if (levelName.equalsIgnoreCase(PmsLevelAndAction.REPORTING_MANAGER.getLabel())) {
                List<PmsProcess> pmsProcessList = updatedPmsData.getPmsProcess();
                List<PmsProcess> processList = pms.getPmsProcess();
                if (!pmsProcessList.isEmpty()) {
                     pmsProcessToUpdate = processList.stream().peek(pmsProcess -> {
                        Optional<PmsProcess> pmsProcessOptional = pmsProcessList.stream()
                                .filter(filterData -> filterData.getKeyBusinessDriver().equalsIgnoreCase(pmsProcess.getKeyBusinessDriver()))
                                .findFirst();
                        if (pmsProcessOptional.isPresent()) {
                            pmsProcess.setLevelRemarks(pmsProcessOptional.get().getLevelRemarks());
                            pmsProcess.setLevelRatingValue(pmsProcessOptional.get().getLevelRatingValue());
                        }
                    }).toList();
                    pms.setPmsProcess(pmsProcessToUpdate);

                }
            }
            pmsModifyRepository.updatePmsData(
                    updatedPmsData, hierarchyLevel, actionType, finalRating, finalRatingValue, pmsProcessToUpdate, status, jwtHelper.getOrganizationCode(), mongoTemplate);
        }
    }

    public String fetchFinalRatingValueByLevel(String level, List<String> finalRating, List<String> finalRatingValue) {
        int index = finalRating.indexOf(level);
        if (index != -1 && index < finalRatingValue.size()) {
            return finalRatingValue.get(index);
        } else {
            return "Rating not found for " + level;
        }
    }

    private List<String> updateFinalRatingValue(List<String> ratings, List<String> finalRatingValue, String newValue) {
        if (finalRatingValue == null) {
            finalRatingValue = new ArrayList<>();
        }

        finalRatingValue.clear();
        for (int i = 0; i < ratings.size(); i++) {
            finalRatingValue.add(newValue);
        }

        return finalRatingValue;
    }

    private List<String> updateHierarchyLevel(List<String> hierarchyLevel, List<String> actionType, String levelName, String action) {
        hierarchyLevel = getOrInitializeList(hierarchyLevel);
        actionType = getOrInitializeList(actionType);

        int index = hierarchyLevel.indexOf(levelName);
        if (index >= 0) {
            actionType.set(index, action);
        } else {
            hierarchyLevel.add(levelName);
            actionType.add(action);
        }
        return hierarchyLevel;
    }

    private List<String> updateActionType(List<String> actionType, List<String> hierarchyLevel, String levelName, String action) {
        actionType = getOrInitializeList(actionType);
        int index = hierarchyLevel.indexOf(levelName);
        if (index != -1) {
            if (actionType.size() > index) {
                actionType.set(index, action);
            } else {
                actionType.add(action);
            }
        }
        return actionType;
    }

    private <T> List<T> getOrInitializeList(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }

    public Map<String, String> keyValueMap(List<String> firstArray, List<String> secondArray) {
        Map<String, String> keyValueMap = new HashMap<>();
        int size = Math.min(firstArray.size(), secondArray.size());
        for (int i = 0; i < size; i++) {
            keyValueMap.put(firstArray.get(i), secondArray.get(i));
        }
        return keyValueMap;
    }
    public String fetchRecommendationByLevel(String level, List<String> recommendation,
                                             List<String> recommendationValue) {
        int index = recommendation.indexOf(level);
        if (index != -1 && index < recommendationValue.size()) {
            return recommendationValue.get(index);
        } else {
            return "Recommendation not found for " + level;
        }
    }



}
