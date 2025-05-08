package com.hepl.budgie.repository.pms;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.dto.pms.LevelDateDTO;
import com.hepl.budgie.entity.pms.LevelDetails;
import com.hepl.budgie.entity.pms.PmsLevel;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PmsLevelMasterRepository extends MongoRepository<PmsLevel,String> {
    public final String COLLECTION_NAME = "pmslevel";
    default Optional<PmsLevel> findLatestLevel(String org, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("levelId").regex("^L")); // Matches strings starting with 'L'
        query.with(Sort.by(Sort.Direction.DESC, "levelId")); // Sort by levelId descending
        query.limit(1); // Get the first result

        PmsLevel latestLevel = mongoTemplate.findOne(query, PmsLevel.class, getCollectionName(org));
        return Optional.ofNullable(latestLevel);
    }

    default void addPmsLevel(PmsLevel pmsLevel, String org, MongoTemplate mongoTemplate){
        mongoTemplate.insert(pmsLevel, getCollectionName(org));
    }

    default void updatePmsFlow(PmsLevel level, String organizationCode, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("levelId").is(level.getId()));

        Update update = new Update()
                .set("showFlow", level.getShowFlow())
                .set("pending", level.getPending())
                .set("completed", level.getCompleted());

        mongoTemplate.updateFirst(query, update, PmsLevel.class, getCollectionName(organizationCode));
    }

    default PmsLevel findById(String id, String organizationCode, MongoTemplate mongoTemplate){
        Query query = new Query(Criteria.where("levelId").is(id));
        PmsLevel level = mongoTemplate.findOne(query, PmsLevel.class, getCollectionName(organizationCode));

        if (level == null) {
            throw new RuntimeException("Id not found");
        }
        return level;
    }
    default void updateLevelDetails(String id, String levelType, LevelDetails levelDetails, String org, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("levelId").is(id));


        Update update = new Update()
                .set("levelType", levelType)
                .set("levelDetails", levelDetails);

        mongoTemplate.updateFirst(query, update, PmsLevel.class, getCollectionName(org));
    }
    default void updateLevelStartAndOut(String levelId, List<String> btnLabels, ZonedDateTime actionTimeStart, ZonedDateTime actionTimeOut, String org, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("levelId").is(levelId));
        Update update = new Update()
                .set("btnLabel", btnLabels)
                .set("actionTimeStart",actionTimeStart)
                .set("actionTimeOut", actionTimeOut);

        mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true),
                PmsLevel.class,getCollectionName(org));
    }
    default List<LevelDateDTO> getAllPmsLevelsAsDTO(String org, MongoTemplate mongoTemplate) {
        List<PmsLevel> pmsLevels = mongoTemplate.findAll(PmsLevel.class, getCollectionName(org));

        return pmsLevels.stream().map(pmsLevel -> {
            LevelDateDTO dto = new LevelDateDTO();
            dto.setLevelName(pmsLevel.getLevelName());
            dto.setAction(pmsLevel.getBtnLabel());
            dto.setActionTimeOut(pmsLevel.getActionTimeOut());
            return dto;
        }).collect(Collectors.toList());
    }
    default Optional<PmsLevel> findByLevelId(String levelId, String org, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("levelId").is(levelId));
        return Optional.ofNullable(mongoTemplate.findOne(query, PmsLevel.class, getCollectionName(org)));
    }
    default Optional<PmsLevel> findByLevelName(String levelName, String org,MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("levelName").is(levelName));
        return Optional.ofNullable(mongoTemplate.findOne(query, PmsLevel.class, getCollectionName(org)));
    }
    default PmsLevel findByLevelNameAndAction(String levelName, String action, String org, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("levelName").is(levelName).and("action").is(action));

        PmsLevel level = mongoTemplate.findOne(query, PmsLevel.class, getCollectionName(org));
        if (level == null) {
            throw new CustomResponseStatusException(AppMessages.LEVEL_NOT_FOUND, HttpStatus.NOT_FOUND,
                    null);
        }
        return level;
    }
    default List<PmsLevel> findByActiveStatus(int activeStatus, MongoTemplate mongoTemplate, String org) {
        Query query = new Query(Criteria.where("activeStatus").is(activeStatus));
        return mongoTemplate.find(query, PmsLevel.class, getCollectionName(org));
    }
    default String getCollectionName(String org){
        return COLLECTION_NAME+(org.isEmpty()?"" : "_"+org);
    }

}
