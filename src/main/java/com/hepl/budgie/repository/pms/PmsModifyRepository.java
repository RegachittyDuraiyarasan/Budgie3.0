package com.hepl.budgie.repository.pms;

import com.hepl.budgie.dto.pms.PmsDTO;
import com.hepl.budgie.entity.pms.Pms;
import com.hepl.budgie.entity.pms.PmsListDTO;
import com.hepl.budgie.entity.pms.PmsProcess;
import com.hepl.budgie.entity.userinfo.UserInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.*;
import java.util.stream.Collectors;

public interface PmsModifyRepository extends MongoRepository<Pms, String> {
    public final String COLLECTION_NAME = "pms";

    default void saveOrUpdate(Pms pms, String org, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(pms.getEmpId()).and("pmsYear").is(pms.getPmsYear()));

        Update update = new Update();
        update.set("status", pms.getStatus());
        update.set("pmsProcess", pms.getPmsProcess());
        update.set("consolidatedSelfRating", pms.getConsolidatedSelfRating());
        update.set("hierarchyLevel", pms.getHierarchyLevel());
        update.set("actionType", pms.getActionType());
        update.set("orgMailStatus", pms.getOrgMailStatus());
        update.set("pmsEmployeeDetails", pms.getPmsEmployeeDetails());

        // Perform upsert (update if exists, insert if not)
        mongoTemplate.upsert(query, update, Pms.class, getCollectionName(org));
    }

    default Pms findByEmpIdAndPmsYear(String empId, String pmsYear, String org, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("pmsYear").is(pmsYear));
        return mongoTemplate.findOne(query, Pms.class, getCollectionName(org));
    }
    default List<Pms> findDataByLevel(PmsListDTO request, List<String> status, String authenticatedEmpId, String organization, MongoTemplate mongoTemplate) {
        Criteria criteria = new Criteria();
        criteria = criteria.and("status").in(status);
        if ("Employee".equals(request.getLevel())) {
            criteria.and("empId").is(authenticatedEmpId);
            criteria.and("pmsYear").is(request.getPmsYear());
            Query query = new Query(criteria);
            return mongoTemplate.find(query, Pms.class, getCollectionName(organization));
        }
        return Collections.emptyList();
    }

    default List<Pms> findDataByReportingManager(
            PmsListDTO request,
            String authenticatedEmpId,
            List<String> status,
            MongoTemplate mongoTemplate,
            String organization
    ) {
        Criteria RMcriteria = Criteria.where("employeeDetails.repManagerId").is(authenticatedEmpId);
        Query rmQuery = new Query(RMcriteria);
        List<UserInfo> userHRInfoList = mongoTemplate.find(rmQuery, UserInfo.class);
        List<String> empIds = userHRInfoList.stream()
                .map(UserInfo::getEmpId)
                .collect(Collectors.toList());

        if (empIds.isEmpty()) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("empId").in(empIds)
                .and("status").in(status);

        if (request.getRoleOfIntake() != null && !request.getRoleOfIntake().isEmpty()) {
            criteria.and("userInfo.sections.workingInformation.roleOfIntake").is(request.getRoleOfIntake());
        }
        if (request.getEmpId() != null && !request.getEmpId().isEmpty()) {
            if (empIds.contains(request.getEmpId())) {
                criteria.and("empId").is(request.getEmpId());
            } else {
                return Collections.emptyList(); // Requested empId is not under this manager
            }
        }
        if (request.getPmsYear() != null && !request.getPmsYear().isEmpty()) {
            criteria.and("pmsYear").is(request.getPmsYear());
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Pms.class, getCollectionName(organization));
    }


    default void updatePmsData(PmsDTO updatedPmsData,
                               List<String> hierarchyLevel,
                               List<String> actionType,
                               List<String> finalRating,
                               List<String> finalRatingValue,
                               List<PmsProcess> pmsProcessToUpdate,
                               String status,
                               String org,
                               MongoTemplate mongoTemplate) {

        Query query = new Query(Criteria.where("empId").is(updatedPmsData.getEmpId())
                .and("pmsYear").is(updatedPmsData.getPmsYear()));

        Update update = new Update()
                .set("hierarchyLevel", hierarchyLevel)
                .set("actionType", actionType)
                .set("finalRating", finalRating)
                .set("finalRatingValue", finalRatingValue)
                .set("pmsProcess", pmsProcessToUpdate)
                .set("status", status)
                .set("overAllRating", updatedPmsData.getFinalRatingValue());

        mongoTemplate.updateFirst(query, update, Pms.class, getCollectionName(org));
    }
    default List<Pms> findDataByReviewer(
            PmsListDTO request,
            String authenticatedEmpId,
            String primaryReportingManager,
            MongoTemplate mongoTemplate,
            String organization
    ) {
        Criteria criteria = Criteria.where("employeeDetails.reviewerId").is(authenticatedEmpId);

        if (primaryReportingManager != null && !primaryReportingManager.isEmpty()) {
            criteria.and("employeeDetails.repManagerId").is(primaryReportingManager);
        }
        if (request.getRoleOfIntake() != null && !request.getRoleOfIntake().isEmpty()) {
            criteria.and("employeeDetails.roleOfIntake").is(request.getRoleOfIntake());
        }
        if (request.getEmpId() != null && !request.getEmpId().isEmpty()) {
            criteria.and("empId").is(request.getEmpId());
        }
        if (request.getPmsYear() != null && !request.getPmsYear().isEmpty()) {
            criteria.and("pmsYear").is(request.getPmsYear());
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Pms.class, getCollectionName(organization));
    }

    default List<Pms> findPmsByEmpIds(List<String> empIds, MongoTemplate mongoTemplate, String org) {
        Query query = new Query(Criteria.where("empId").in(empIds));
        return mongoTemplate.find(query, Pms.class, getCollectionName(org));
    }
    default List<String> findEmpIdsByRepManagerId(String repManagerId, String org, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("pmsEmployeeDetails.repManagerId").is(repManagerId));
        query.fields().include("empId");

        String collectionName = getCollectionName(org); // Ensure this returns "pms_ORG00001"
        System.out.println("Fetching from collection: " + collectionName); // Debugging statement

        return mongoTemplate.find(query, Pms.class, collectionName)
                .stream()
                .map(Pms::getEmpId)
                .collect(Collectors.toList());
    }


    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }



}
