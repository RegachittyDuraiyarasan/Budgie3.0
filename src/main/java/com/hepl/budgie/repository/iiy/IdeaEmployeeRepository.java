package com.hepl.budgie.repository.iiy;

import com.hepl.budgie.dto.iiy.IdeaEmployeeRequestDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.IIYDetails;
import com.hepl.budgie.entity.iiy.IdeaDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface IdeaEmployeeRepository extends MongoRepository<IdeaDetails, String> {
    public static final String COLLECTION_NAME = "idea_details";

    default boolean existsByEmpIdAndIdea(MongoTemplate mongoTemplate, String organizationCode, String empId,
            String idea) {
        Query query = new Query(Criteria.where("empId").is(empId).and("idea").is(idea));
        return mongoTemplate.exists(query, IdeaDetails.class, getCollectionName(organizationCode));
    }

    default void insertOrUpdate(IdeaEmployeeRequestDTO request, MongoTemplate mongoTemplate, String organizationCode,
            String authUser, String id, String type) {
        Query query = new Query(Criteria.where("ideaId").is(id).and("empId").is(request.getEmpId()));
        boolean isNew = !mongoTemplate.exists(query, IdeaDetails.class, getCollectionName(organizationCode));
        Update update = new Update();
        if (type.equalsIgnoreCase("approve")) {
            update.set("rmStatus", Status.APPROVED.label);
            update.set("rmRemarks", request.getRmRemarks());
            update.set("rmWeightage", request.getRmWeightage());

        } else if (type.equalsIgnoreCase("reject")) {
            update.set("rmStatus", Status.REJECTED.label);
            update.set("rmRemarks", request.getRmRemarks());
            update.set("rmWeightage", request.getRmWeightage());

        } else {
            update.set("financialYear", request.getFinancialYear());
            update.set("ideaDate", request.getIdeaDateValue());
            update.set("idea", request.getIdea());
            update.set("course", request.getCourse());
            update.set("category", request.getCategory());
            update.set("weightage", request.getWeightage());
            update.set("description", request.getDescription());
            update.set("certification", request.getDepartment());
        }
        if (isNew) {
            update.setOnInsert("empId", request.getEmpId());
            update.setOnInsert("rmStatus", Status.PENDING.label);
        }
        update = auditInfo(update, isNew, authUser);
        mongoTemplate.upsert(query, update, IdeaDetails.class, getCollectionName(organizationCode));
    }

    default List<IdeaDetails> fetchIdeaReportByFilters(MongoTemplate mongoTemplate, String organizationCode,
            IdeaEmployeeRequestDTO data) {
        String reportingManagerEmpId = data.getReportingManagerEmpId();
        String empId = data.getEmpId();
        String fromDate = data.getFromDate();
        String toDate = data.getToDate();
        String department = data.getDepartment();
        String course = data.getCourse();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime from = null;
        LocalDateTime to = null;
        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDateTime.parse(fromDate + " 00:00:00", formatter);
        }
        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDateTime.parse(toDate + " 23:59:59", formatter);
        }
        Query employeequery = new Query();
        if (reportingManagerEmpId != null && !reportingManagerEmpId.isEmpty()) {
            employeequery.addCriteria(Criteria.where("reportingManagerEmpId").is(reportingManagerEmpId));
        }
        if (department != null && !department.isEmpty()) {
            employeequery.addCriteria(Criteria.where("department").is(department));
        }

        List<UserInfo> employeeDetails = mongoTemplate.find(employeequery, UserInfo.class);
        // Extract empId
        List<String> empIdsUnderRepMan = new ArrayList<>();
        for (UserInfo detail : employeeDetails) {
            empIdsUnderRepMan.add(detail.getEmpId());
        }
        Query query = new Query();

        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("empId").is(empId));
        } else {
            query.addCriteria(Criteria.where("empId").in(empIdsUnderRepMan));
        }
        if (course != null && !course.isEmpty()) {
            query.addCriteria(Criteria.where("course").is(course));
        }
        if ((fromDate != null && !fromDate.isEmpty()) && (toDate != null && !toDate.isEmpty())) {
            query.addCriteria(Criteria.where("ideaDate").gte(from).lte(to));
        }

        return mongoTemplate.find(query, IdeaDetails.class);
    }

    IdeaDetails findByEmpIdAndIdea(String empId, String idea);

    default List<IdeaDetails> findByEmpIdAndFromDateAndToDate(MongoTemplate mongoTemplate, String organizationCode,
            String empId, LocalDateTime fromDate, LocalDateTime toDate) {
        Query query = new Query(Criteria.where("empId").is(empId).and("iiyDate").gte(fromDate).lte(toDate));
        return mongoTemplate.find(query, IdeaDetails.class, getCollectionName(organizationCode));

    }

    default List<IdeaDetails> fetchIdeaByFilters(MongoTemplate mongoTemplate, String organizationCode,
            IdeaEmployeeRequestDTO data) {
        String reportingManagerEmpId = data.getReportingManagerEmpId();
        String empId = data.getEmpId();
        String ideaDate = data.getIdeaDate();
        String rmStatus = Status.PENDING.label;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime from = null;
        LocalDateTime to = null;

        if (ideaDate != null && !ideaDate.isEmpty()) {
            from = LocalDateTime.parse(ideaDate + " 00:00:00", formatter);
            to = LocalDateTime.parse(ideaDate + " 23:59:59", formatter);
        }
        Query employeequery = new Query();
        if (reportingManagerEmpId != null && !reportingManagerEmpId.isEmpty()) {
            employeequery.addCriteria(Criteria.where("reportingManagerEmpId").is(reportingManagerEmpId));
        }

        List<UserInfo> employeeDetails = mongoTemplate.find(employeequery, UserInfo.class);

        // Extract empId
        List<String> empIdsUnderRepMan = new ArrayList<>();
        for (UserInfo detail : employeeDetails) {
            empIdsUnderRepMan.add(detail.getEmpId());
        }
        Query query = new Query();

        query.addCriteria(Criteria.where("rmStatus").is(rmStatus));

        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("empId").is(empId));
        } else {
            query.addCriteria(Criteria.where("empId").in(empIdsUnderRepMan));
        }

        if (ideaDate != null && !ideaDate.isEmpty()) {
            query.addCriteria(Criteria.where("ideaDate").gte(from).lte(to));
        }

        return mongoTemplate.find(query, IdeaDetails.class, getCollectionName(organizationCode));
    }

    default IdeaDetails findByIdAndEmpId(MongoTemplate mongoTemplate, String organizationCode, String id,
            String empId) {
        Query query = new Query(Criteria.where("_id").is(id).and("empId").is(empId));
        return mongoTemplate.findOne(query, IdeaDetails.class, getCollectionName(organizationCode));
    }

    default Optional<IdeaDetails> findTopByOrderByIdDesc(String organizationCode, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        IdeaDetails result = mongoTemplate.findOne(query, IdeaDetails.class,
                getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default Update auditInfo(Update update, boolean isNew, String authUser) {

        if (isNew) {
            update.setOnInsert("createdDate", LocalDateTime.now());
            update.setOnInsert("createdByUser", authUser);
        }
        update.set("lastModifiedDate", LocalDateTime.now());
        update.set("modifiedByUser", authUser);
        return update;
    }

}
