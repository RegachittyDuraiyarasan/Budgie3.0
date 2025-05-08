package com.hepl.budgie.service.impl.pms;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.entity.pms.Pms;
import com.hepl.budgie.repository.master.ModuleMasterSettingsRepository;
import com.hepl.budgie.repository.pms.PmsModifyRepository;
import com.hepl.budgie.service.pms.PmsAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PmsAnalyticsServiceImplementation implements PmsAnalyticsService {
    private final PmsModifyRepository pmsModifyRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final ModuleMasterSettingsRepository moduleMasterSettingsRepository;
    @Override
    public List<Map<String, String>> fetchChartData(String pmsYear, String levelName) {

        List<String> pmsEligibleEmpIdsList = pmsModifyRepository.findEmpIdsByRepManagerId(jwtHelper.getUserRefDetail().getEmpId(), jwtHelper.getOrganizationCode(), mongoTemplate);
        List<Pms> pmsInfoList = pmsModifyRepository.findPmsByEmpIds(pmsEligibleEmpIdsList, mongoTemplate, jwtHelper.getOrganizationCode());
        log.info("pmsInfoList{}",pmsInfoList);
        List<Pms> pmsInfoByYear = pmsInfoList.stream()
                .filter(pmsInfo -> pmsInfo.getPmsYear().equals(pmsYear))
                .toList();

        // Map to store empId -> {ratingType -> ratingValue}
        Map<String, Map<String, String>> ratingMapByEmpId = new HashMap<>();
        for (Pms pms : pmsInfoByYear) {
            String empId = pms.getEmpId();
            List<String> finalRating = Optional.ofNullable(pms.getFinalRating()).orElse(new ArrayList<>());
            List<String> finalRatingValue = pms.getFinalRatingValue();

            Map<String, String> ratingMap = new HashMap<>();
            for (int i = 0; i < finalRating.size(); i++) {
                ratingMap.put(finalRating.get(i), finalRatingValue.get(i));
            }
            ratingMapByEmpId.put(empId, ratingMap);
        }
        log.info("ratingMapByEmpId{}",ratingMapByEmpId);
        // Extract ratings for the given level
        Map<String, String> reportingManagerRatings = ratingMapByEmpId.entrySet().stream()
                .filter(e -> e.getValue().containsKey(levelName))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(levelName)));
        Optional<ModuleMaster> moduleMasterOpt = moduleMasterSettingsRepository.findByReferenceName("Pms RatingMaster", jwtHelper.getOrganizationCode(), mongoTemplate);

        // List to store team members with their ratings
        List<Map<String, String>> teamMembers = new ArrayList<>();
        assert pmsEligibleEmpIdsList != null;
        for (String empId : pmsEligibleEmpIdsList) {
            String rating = reportingManagerRatings.getOrDefault(empId, "");
            String ratingNumber = "0";

            if (!rating.isEmpty() && moduleMasterOpt.isPresent()) {
                ModuleMaster moduleMaster = moduleMasterOpt.get();

                Optional<Map<String, Object>> ratingDetailOpt = moduleMaster.getOptions().stream()
                        .filter(opt -> opt.containsKey("ratingName") && rating.equals(opt.get("ratingName").toString()))
                        .findFirst();

                if (ratingDetailOpt.isPresent()) {
                    Map<String, Object> ratingDetail = ratingDetailOpt.get();
                    if (ratingDetail.containsKey("percentage")) {
                        ratingNumber = String.valueOf(ratingDetail.get("percentage"));
                    }
                }
            }

            Map<String, String> field = new HashMap<>();
            field.put("empId", empId);
            field.put("rating", rating);
            field.put("ratingNumber", ratingNumber);
            teamMembers.add(field);
        }
        // Sort team members by rating number
        teamMembers.sort(Comparator.comparingInt(o -> Integer.parseInt(o.get("ratingNumber"))));

        List<Map<String, String>> array1 = new ArrayList<>();
        List<Map<String, String>> array2 = new ArrayList<>();

        for (int i = 0; i < teamMembers.size(); i++) {
            Map<String, String> element = teamMembers.get(i);
            if (i % 2 == 0) {
                array1.add(0, element);
            } else {
                array2.add(element);
            }
        }

        List<Map<String, String>> mergedArray = new ArrayList<>(array2);
        mergedArray.addAll(array1);
        return mergedArray;
    }

}
