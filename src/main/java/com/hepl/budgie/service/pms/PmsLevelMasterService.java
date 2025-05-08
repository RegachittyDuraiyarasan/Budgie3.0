package com.hepl.budgie.service.pms;

import com.hepl.budgie.dto.pms.LevelDateDTO;
import com.hepl.budgie.dto.pms.LevelFlowUpdateDTO;
import com.hepl.budgie.entity.pms.LevelDetails;
import com.hepl.budgie.entity.pms.PmsLevel;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

public interface PmsLevelMasterService {

    void addPmsLevel(PmsLevel request);

    void updateFlow(LevelFlowUpdateDTO request);

    void updateLevelDetails(LevelDetails request);

    void updateLevelTime(LevelDetails request);

    List<LevelDateDTO> getPmsLevelTime();

    String fetchStatusPmsRequest(String levelName, String status);

    String fetchCountPmsRequest(String levelName, String action);

    List<String> fetchFlowPmsRequest(String levelData, String org, MongoTemplate mongoTemplate);
}
