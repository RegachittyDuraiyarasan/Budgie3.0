package com.hepl.budgie.service.impl.pms;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.pms.LevelDateDTO;
import com.hepl.budgie.dto.pms.LevelFlowUpdateDTO;
import com.hepl.budgie.entity.pms.LevelDetails;
import com.hepl.budgie.entity.pms.PmsLevel;
import com.hepl.budgie.entity.pms.PmsWorkflow;
import com.hepl.budgie.repository.pms.PmsLevelMasterRepository;
import com.hepl.budgie.service.pms.PmsLevelMasterService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PmsLevelMasterServiceImplementation implements PmsLevelMasterService {
    private final PmsLevelMasterRepository pmsLevelMasterRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public void addPmsLevel(PmsLevel request) {
        String levelId = generateLevelId();
        int size = request.getAction().size();
        //For Number Action  Status without Deviation
        int startValue = actionStatus();
        List<Integer> actionIntArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            actionIntArray.add(startValue + i);
        }
        PmsLevel pmsLevel = new PmsLevel();
        pmsLevel.setLevelId(levelId);
        pmsLevel.setLevelName(request.getLevelName());
        pmsLevel.setAction(request.getAction());
        pmsLevel.setActionStatus(actionIntArray);
        pmsLevelMasterRepository.addPmsLevel(pmsLevel, jwtHelper.getOrganizationCode(), mongoTemplate);
    }

    @Override
    public void updateFlow(LevelFlowUpdateDTO request) {
        PmsLevel level = pmsLevelMasterRepository.findById(request.getLevelId(), jwtHelper.getOrganizationCode(), mongoTemplate);
        log.info("level{}", level);
        PmsWorkflow showFlow = new PmsWorkflow(request.getShowType(), request.getStatus());
        PmsWorkflow compFlow = new PmsWorkflow(request.getCompletedType(), request.getCompletedStatus());
        PmsWorkflow penFlow = new PmsWorkflow(request.getPendingType(), request.getPendingStatus());

        level.setShowFlow(showFlow);
        level.setPending(penFlow);
        level.setCompleted(compFlow);

        pmsLevelMasterRepository.updatePmsFlow(level, jwtHelper.getOrganizationCode(), mongoTemplate);
    }

    @Override
    public void updateLevelDetails(LevelDetails data) {
        LevelDetails levelDetails = new LevelDetails();

        if (!"Final".equals(data.getLevelType())) {
            if (data.getNextLevel().size() != data.getNextLevelCondition().size()) {
                throw new RuntimeException("Size is not matching");
            }
            levelDetails.setNextLevel(data.getNextLevel());
            levelDetails.setNextLevelCondition(data.getNextLevelCondition());
        }
        if (!"Initial".equals(data.getLevelType())) {
            levelDetails.setPreviousLevel(data.getPreviousLevel());
        }

        levelDetails.setActionCount(data.getActionCount());
        levelDetails.setMandatory(data.getMandatory());
        levelDetails.setProgressStatus(data.getProgressStatus());
        levelDetails.setBasedOn(data.getBasedOn());
        pmsLevelMasterRepository.updateLevelDetails(data.getLevelId(), data.getLevelType(), levelDetails, jwtHelper.getOrganizationCode(), mongoTemplate);

    }

    @Override
    public void updateLevelTime(LevelDetails request) {
        pmsLevelMasterRepository.updateLevelStartAndOut(
                request.getLevelId(),
                request.getBtnLabel(),
                request.getBtnTimeStart(),
                request.getBtnTimeOut(),
                jwtHelper.getOrganizationCode(),
                mongoTemplate
        );

    }

    @Override
    public List<LevelDateDTO> getPmsLevelTime() {
        return pmsLevelMasterRepository.getAllPmsLevelsAsDTO(jwtHelper.getOrganizationCode(), mongoTemplate);
    }

    private String generateLevelId() {
        Optional<PmsLevel> existingLevel = pmsLevelMasterRepository.findLatestLevel(jwtHelper.getOrganizationCode(), mongoTemplate);
        return existingLevel.isPresent()
                ? generateNextID("L", existingLevel.get().getLevelId())
                : "L1";
    }

    public String generateNextID(String prefix, String existingIDs) {
        // Split the existing IDs by a separator (e.g., comma) if needed
        String[] existingIDsArray = existingIDs.split(",");

        String maxExistingID = Arrays.stream(existingIDsArray)
                .filter(id -> id.startsWith(prefix))
                .max(String::compareTo)
                .orElse(prefix + "0");

        int empNumber = Integer.parseInt(maxExistingID.substring(prefix.length()));
        int nextNumber = empNumber + 1;

        // Calculate the maximum number of digits needed for the numeric part
        int maxDigits = Math.max((int) Math.log10(nextNumber) + 1, 1);

        return prefix + String.format("%0" + maxDigits + "d", nextNumber);
    }

    private int actionStatus() {
        Integer existingLevel = findMaxActionStatus();
        return (existingLevel == null) ? 0 : existingLevel + 1;
    }

    public Integer findMaxActionStatus() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("actionStatus"),
                Aggregation.sort(Sort.Direction.DESC, "actionStatus"),
                Aggregation.limit(1)
        );

        AggregationResults<PmsLevel> result = mongoTemplate.aggregate(aggregation, "pmsLevel", PmsLevel.class);

        if (!result.getMappedResults().isEmpty()) {
            int size = result.getMappedResults().get(0).getActionStatus().size();
            return result.getMappedResults().get(0).getActionStatus().get(size - 1);
        }

        return null;
    }
    @Override
    public String fetchStatusPmsRequest(String levelName, String status) {
        PmsLevel level = pmsLevelMasterRepository.findByLevelName(levelName, jwtHelper.getOrganizationCode(), mongoTemplate)
                .orElseThrow(() -> new CustomResponseStatusException(AppMessages.LEVEL_NOT_FOUND, HttpStatus.NOT_FOUND, null));

        if (!level.getAction().contains(status)) {
            throw new CustomResponseStatusException(AppMessages.ACTION_NOT_FOUND, HttpStatus.NOT_FOUND, null);
        }

        LevelDetails levelDetails = level.getLevelDetails();
        Map<String, String> nextLevelMap = getNextLevelMap(levelDetails);

        // Get the next level ID
        String nextLevelForAction = nextLevelMap.keySet().stream().findFirst().orElse(null);
        if (nextLevelForAction == null) {
            throw new CustomResponseStatusException(AppMessages.NEXT_LEVEL_NOT_FOUND, HttpStatus.NOT_FOUND, null);
        }

        // Fetch next level details
        PmsLevel nextLevelData = pmsLevelMasterRepository.findByLevelId(nextLevelForAction, jwtHelper.getOrganizationCode(), mongoTemplate)
                .orElseThrow(() -> new CustomResponseStatusException(AppMessages.NEXT_LEVEL_NOT_FOUND, HttpStatus.NOT_FOUND, null));

        Map<String, String> resultMap = nextLevelData.getActiveStatus() == 1
                ? getActionMap(level)
                : getActionMap(nextLevelData);

        String action = nextLevelData.getAction().contains("Approve") ? "Approve" : "Submit";
        if (resultMap.containsKey(status)) {
            return resultMap.get(status);
        } else if (resultMap.containsKey(action)) {
            return resultMap.get(action);
        } else {
            throw new CustomResponseStatusException(AppMessages.ACTION_NOT_FOUND, HttpStatus.NOT_FOUND, null);
        }
    }

    private Map<String, String> getNextLevelMap(LevelDetails levelDetails) {
        Map<String, String> nextLevelMap = new HashMap<>();
        List<String> nextLevel = levelDetails.getNextLevel();
        List<String> nextLevelCondition = levelDetails.getNextLevelCondition();

        for (int i = 0; i < nextLevel.size(); i++) {
            nextLevelMap.put(nextLevel.get(i), nextLevelCondition.get(i));
        }
        return nextLevelMap;
    }

    private static Map<String, String> getActionMap(PmsLevel level) {

        List<String> keys = level.getAction();
        List<?> values;
        values = level.getActionStatus();

        // Check if the number of elements in both arrays is the same
        Map<String, String> resultMap = new HashMap<>();

        for (int i = 0; i < keys.size(); i++) {
            resultMap.put(keys.get(i), String.valueOf(values.get(i)));
        }
        return resultMap;
    }
    @Override
    public String fetchCountPmsRequest(String levelName, String action) {
        PmsLevel level = pmsLevelMasterRepository.findByLevelNameAndAction(levelName, action, jwtHelper.getOrganizationCode(), mongoTemplate);
        if (level == null) {
            return null;
        }

        List<String> actionNames = level.getAction();
        List<String> actionCounts = level.getLevelDetails().getActionCount();

        Map<String, String> actionCountMap = new HashMap<>();
        for (int i = 0; i < actionNames.size(); i++) {
            actionCountMap.put(actionNames.get(i), actionCounts.get(i));
        }
        String count = actionCountMap.get(action);
        if (count != null) {
            return count;
        } else {
            return "Action count not available for this action";
        }

    }
    @Override
    public List<String> fetchFlowPmsRequest(String levelData, String org, MongoTemplate mongoTemplate){
        Optional<PmsLevel> level = pmsLevelMasterRepository.findByLevelName(levelData, org, mongoTemplate);
        PmsWorkflow showFlow=level.get().getShowFlow();
        if(showFlow==null){
            throw new RuntimeException("Fetch Flow is empty");
        }
        List<String> outputArray = new ArrayList<>();
        if (Objects.equals(showFlow.getType(), "Range")) {
            int start = Integer.parseInt(showFlow.getStatus().get(0)); //contains 1
            int end = Integer.parseInt(showFlow.getStatus().get(1)); //contains
            List<Integer> result = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                result.add(i);
            }
            for (Integer intValue : result) {
                outputArray.add(String.valueOf(intValue));
            }
        }else{
            outputArray=showFlow.getStatus();

        }
        return outputArray;

    }


}
