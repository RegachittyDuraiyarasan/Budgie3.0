package com.hepl.budgie.repository.separation;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.separation.SeparationExitInfo;
@Repository
public interface SeparationExitInfoRepository extends MongoRepository<SeparationExitInfo, String> {

    String COLLECTION_NAME = "separation_exit_info";

    default String getCollectionName(String org) {
        return (org == null || org.isEmpty()) ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default SeparationExitInfo upsertSeparationExitInfo(String orgCode, SeparationExitInfo separationExitInfo, MongoTemplate mongoTemplate) {
        String collectionName = getCollectionName(orgCode);
        String separationCollectionName = "separationInfo_"+orgCode;
    
         boolean isNewRecord = false;
        if (separationExitInfo.getId() == null || separationExitInfo.getId().isEmpty()) {
            separationExitInfo.setId(new ObjectId().toHexString());
            isNewRecord = true;
        }
        Query query = new Query(Criteria.where("_id").is(separationExitInfo.getId()));

        Update update = new Update()
                .set("separationId", separationExitInfo.getSeparationId())
                .set("empId", separationExitInfo.getEmpId())
                .set("submittedOn", separationExitInfo.getSubmittedOn() != null ? separationExitInfo.getSubmittedOn() : LocalDateTime.now())
                .set("comments", separationExitInfo.getComments())
                .set("reasons", separationExitInfo.getReasons())
                .set("jobItself", separationExitInfo.getJobItself())
                .set("remunerationAndBenefits", separationExitInfo.getRemunerationAndBenefits())
                .set("supervisorOrManager", separationExitInfo.getSupervisorOrManager())
                .set("company", separationExitInfo.getCompany())
                .set("management", separationExitInfo.getManagement());

                SeparationExitInfo result = mongoTemplate.findAndModify(query, update, 
                FindAndModifyOptions.options().returnNew(true).upsert(true), 
                SeparationExitInfo.class, collectionName);
        
                if (isNewRecord) {
                    Query separationQuery = new Query(Criteria.where("_id").is(separationExitInfo.getSeparationId())); 
                    Update separationUpdate = new Update().set("noDueStatus", Status.APPROVED.label);
                    mongoTemplate.updateFirst(separationQuery, separationUpdate, separationCollectionName);
                } 
        return result;
    }

    default SeparationExitInfo getSeparationExitInfoBySeparationId(String orgCode, String separationId, MongoTemplate mongoTemplate) {
        String collectionName = getCollectionName(orgCode);
        Query query = new Query(Criteria.where("separationId").is(separationId));
        return mongoTemplate.findOne(query, SeparationExitInfo.class, collectionName);
    }
}

