package com.hepl.budgie.repository.gradeMaster;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.GradeMaster;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface GradeMasterRepository extends MongoRepository<GradeMaster, String> {

    public static final String COLLECTION_NAME = "grade";

    public static final String GRADE_SEQUENCE = "G000";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + "_" + org);
    }

    default void saveGrade(GradeMaster gradeMaster, String org, MongoTemplate mongoTemplate) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        if (gradeMaster.getGradeId() == null || gradeMaster.getGradeId().isEmpty()) {
            String nextGradeId = generateNextGradeId(mongoTemplate ,org);
            gradeMaster.setGradeId(nextGradeId);
        }

        Query query = new Query(Criteria.where("gradeId").is(gradeMaster.getGradeId()));

        Update update = savedGradeMaster(gradeMaster);

        mongoTemplate.upsert(query,update, GradeMaster.class, collectionName);
    }


    static Update savedGradeMaster(GradeMaster dto) {
        Update update = new Update()
                .set("gradeId", dto.getGradeId())
                .set("gradeName", dto.getGradeName())
                .set("noticePeriod", dto.getNoticePeriod())
                .set("status", Status.ACTIVE.label)
                .set("probationStatus", dto.getProbationStatus())
                .set("_class", GradeMaster.class.getName());

        // Handling `probationDetail` safely
        if (dto.getProbationDetail() != null) {
            update.set("probationDetail.defaultDurationMonth", dto.getProbationDetail().getDefaultDurationMonth());
            update.set("probationDetail.extendedOptionMonth", dto.getProbationDetail().getExtendedOptionMonth());
        } else {
            update.unset("probationDetail"); // Removes field from MongoDB if null
        }

        return update;
    }


    private String generateNextGradeId(MongoTemplate mongoTemplate, String org) {
        Query query = new Query();
        query.fields().include("gradeId");

        List<String> gradeIds = mongoTemplate.find(query, GradeMaster.class, getCollectionName(org))
                .stream()
                .map(GradeMaster::getGradeId)
                .filter(Objects::nonNull)
                .toList();

        String lastSequence = gradeIds.stream()
                .max(String::compareTo)
                .orElse(GRADE_SEQUENCE);

        return AppUtils.generateUniqueIdExpEdu(lastSequence, 3);
    }

    default Optional<GradeMaster> findByGradeId(String gradeId, String org, MongoTemplate mongoTemplate) {
        String collectionName = "grade_" + org;
        Query query = new Query(Criteria.where("gradeId").is(gradeId));
        GradeMaster result = mongoTemplate.findOne(query, GradeMaster.class, collectionName);
        return Optional.ofNullable(result);
    }

    default void updateGradeOption(GradeMaster gradeMaster, String org, MongoTemplate mongoTemplate) {
        String collectionName = getCollectionName(org);

        Query query = new Query(Criteria.where("gradeId").is(gradeMaster.getGradeId()));

        Update update = savedGradeMaster(gradeMaster);

        if (mongoTemplate.exists(query, GradeMaster.class, collectionName)) {
            mongoTemplate.updateFirst(query, update, GradeMaster.class, collectionName);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.GRADE_ID_NOTFOUND);
        }
    }

    default UpdateResult deleteOptions(String gradeId, MongoTemplate mongoTemplate, String org) {
        String collectionName = getCollectionName(org);
        Query query = new Query(Criteria.where("gradeId").is(gradeId));

        Update update = new Update().set("status", Status.DELETED.label);

        return mongoTemplate.updateFirst(query, update, GradeMaster.class, collectionName);
    }

    default void gradeStatus(String gradeId, MongoTemplate mongoTemplate, String org) {
        String collectionName = getCollectionName(org);
        Query query = new Query(Criteria.where("gradeId").is(gradeId));

        GradeMaster existing = mongoTemplate.findOne(query, GradeMaster.class, collectionName);
        if (existing == null || Status.DELETED.label.equalsIgnoreCase(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.GRADE_ID_NOTFOUND);
        }

        String currentStatus = existing.getStatus();
        String newStatus = Status.ACTIVE.label.equalsIgnoreCase(currentStatus)
                ? Status.INACTIVE.label
                : Status.ACTIVE.label;

        Update update = new Update().set("status", newStatus);
        mongoTemplate.updateFirst(query, update, GradeMaster.class, collectionName);
    }




}
