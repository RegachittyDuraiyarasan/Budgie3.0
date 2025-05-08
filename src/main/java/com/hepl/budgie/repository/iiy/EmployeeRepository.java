package com.hepl.budgie.repository.iiy;

import com.hepl.budgie.dto.iiy.ActivityRequestDTO;
import com.hepl.budgie.dto.iiy.IIYEmployeeRequestDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.Action;
import com.hepl.budgie.entity.iiy.IIYDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;

public interface EmployeeRepository extends MongoRepository<IIYDetails, String> {
        public static final String COLLECTION_NAME = "iiy_details";

        default IIYDetails findByActivityId(MongoTemplate mongoTemplate, String organizationCode, String id) {
                Query query = new Query(Criteria.where("activityId").is(id));
                return mongoTemplate.findOne(query, IIYDetails.class, getCollectionName(organizationCode));
        }

        default IIYDetails findByActivityIdAndEmpId(MongoTemplate mongoTemplate, String organizationCode, String id,
                        String empId) {
                Query query = new Query(Criteria.where("activityId").is(id).and("empId").is(empId));
                return mongoTemplate.findOne(query, IIYDetails.class, getCollectionName(organizationCode));
        }

        default List<IIYDetails> findByEmpId(MongoTemplate mongoTemplate, String organizationCode, String empId) {
                Query query = new Query(Criteria.where("empId").is(empId));
                return mongoTemplate.find(query, IIYDetails.class, getCollectionName(organizationCode));
        }

        default List<IIYDetails> findByEmpIdAndFromDateAndToDate(MongoTemplate mongoTemplate, String organizationCode,
                        String empId, LocalDateTime fromDate, LocalDateTime toDate) {
                Query query = new Query(Criteria.where("empId").is(empId).and("iiyDate").gte(fromDate).lte(toDate));
                return mongoTemplate.find(query, IIYDetails.class, getCollectionName(organizationCode));

        }

        default void insertOrUpdate(ActivityRequestDTO request, MongoTemplate mongoTemplate, String organizationCode,
                        String authUser, String type, String timeZone) {
                Query query = new Query(Criteria.where("activityId").is(request.getActivityId()).and("empId")
                                .is(request.getEmpId()));
                boolean isNew = !mongoTemplate.exists(query, IIYDetails.class, getCollectionName(organizationCode));
                Update update = new Update();
                if (type.equalsIgnoreCase(Action.APPROVE.label)) {
                        update.set("rmStatus", Status.APPROVED.label);
                        update.set("rmRemarks", request.getRmRemarks());

                } else if (type.equalsIgnoreCase(Action.REJECT.label)) {
                        update.set("rmStatus", Status.REJECTED.label);
                        update.set("rmRemarks", request.getRemarks());

                } else {
                        ZonedDateTime parsedIiyDate = null;
                        try {
                                if (request.getIiyDate() == null || request.getIiyDate().trim().isEmpty()) {
                                        // If iiyDate is empty, store today's date
                                        parsedIiyDate = ZonedDateTime.now();
                                } else {
                                        // Parse ISO 8601 date format
                                        parsedIiyDate = AppUtils.parseZonedDate("yyyy-MM-dd HH:mm",
                                                        request.getIiyDate() + " 00:00",
                                                        timeZone); // Convert Instant to
                                                                   // ZonedDateTime
                                }
                        } catch (Exception e) {
                                throw new RuntimeException("Invalid date format for iiyDate: " + request.getIiyDate(),
                                                e);
                        }

                        update.set("financialYear", request.getFinancialYear());
                        // update.set("iiyDate", request.getIiyDate());
                        update.set("iiyDate", parsedIiyDate);
                        update.set("courseCategory", request.getCourseCategory());
                        update.set("course", request.getCourse());
                        update.set("duration", request.getDuration());
                        update.set("remarks", request.getRemarks());
                        update.set("description", request.getDescription());
                        update.set("certification", request.getCertification());
                        update.set("fileName", request.getFileName());
                }
                if (isNew) {
                        update.setOnInsert("empId", request.getEmpId());
                        update.setOnInsert("activityId", request.getActivityId());
                        update.setOnInsert("rmStatus", Status.PENDING.label);
                }
                update = auditInfo(update, isNew, authUser);
                mongoTemplate.upsert(query, update, IIYDetails.class, getCollectionName(organizationCode));
        }

        default List<IIYDetails> fetchActivityByFilters(MongoTemplate mongoTemplate, String organizationCode,
                        IIYEmployeeRequestDTO data) {

                String empId = data.getEmpId();
                String reportingManagerEmpId = data.getReportingManagerEmpId();
                String iiyDate = data.getIiyDate();

                LocalDate from = null;
                LocalDate to = null;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

                if (iiyDate != null && !iiyDate.isEmpty()) {
                        from = LocalDate.parse(iiyDate + " 00:00:00", formatter);
                        to = LocalDate.parse(iiyDate + " 23:59:59", formatter);

                }

                Query employeeQuery = new Query();
                // employeequery.addCriteria(Criteria.where("payrollStatus").is(organizationCode));

                if (reportingManagerEmpId != null && !reportingManagerEmpId.isEmpty()) {
                        employeeQuery.addCriteria(Criteria.where("sections.hrInformation.primary.managerId")
                                        .is(reportingManagerEmpId));
                }

                List<UserInfo> employeeDetails = mongoTemplate.find(employeeQuery, UserInfo.class);
                List<String> empIdsUnderRepMan = new ArrayList<>();
                for (UserInfo detail : employeeDetails) {
                        empIdsUnderRepMan.add(detail.getEmpId());
                }

                Query query = new Query();
                query.addCriteria(Criteria.where("rmStatus").is(Status.PENDING.label));
                if (empId != null && !empId.isEmpty()) {
                        query.addCriteria(Criteria.where("empId").is(empId));
                } else {
                        query.addCriteria(Criteria.where("empId").in(empIdsUnderRepMan));
                }

                if (from != null && to != null) {
                        query.addCriteria(Criteria.where("iiyDate").gte(from).lte(to));
                }

                List<IIYDetails> results = mongoTemplate.find(query, IIYDetails.class,
                                getCollectionName(organizationCode));

                return results;
        }

        default List<IIYDetails> fetchActivityReportByFilters(MongoTemplate mongoTemplate, String organizationCode,
                        IIYEmployeeRequestDTO data) {
                String reportingManagerEmpId = data.getReportingManagerEmpId();
                String empId = data.getEmpId();
                String fromDate = data.getFromDate();
                String toDate = data.getToDate();
                String department = data.getDepartment();
                String divisionHeadEmpId = data.getDivisionHeadId();
                String rmStatus = data.getRmStatus();
                String status = data.getStatus();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                LocalDate from = null;
                LocalDate to = null;
                LocalDate fifteenthDate = null;
                // Get current date and 15th of the current month
                LocalDate currentDate = LocalDate.now();
                LocalDate fifteenthOfMonth = currentDate.withDayOfMonth(15);

                // Format to "dd-MM-yyyy"
                DateTimeFormatter patternFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String fifteenthOfMonthStr = fifteenthOfMonth.format(patternFormatter);
                if (fifteenthOfMonthStr != null && !fifteenthOfMonthStr.isEmpty()) {
                        fifteenthDate = LocalDate.parse(fifteenthOfMonthStr + " 23:59:59", formatter);
                }
                if (fromDate != null && !fromDate.isEmpty()) {
                        from = LocalDate.parse(fromDate + " 00:00:00", formatter);
                }
                if (toDate != null && !toDate.isEmpty()) {
                        to = LocalDate.parse(toDate + " 23:59:59", formatter);
                }
                Query employeequery = new Query();
                // employeequery.addCriteria(Criteria.where("payrollStatus").is(organizationCode));

                if (reportingManagerEmpId != null && !reportingManagerEmpId.isEmpty()) {
                        employeequery.addCriteria(
                                        Criteria.where("sections.hrInformation.primary.managerId")
                                                        .is(reportingManagerEmpId));
                }
                if (department != null && !department.isEmpty()) {
                        employeequery.addCriteria(
                                        Criteria.where("sections.workingInformation.department").is(department));
                }
                if (divisionHeadEmpId != null && !divisionHeadEmpId.isEmpty()) {
                        employeequery.addCriteria(
                                        Criteria.where("sections.hrInformation.divisionHead.managerId")
                                                        .is(divisionHeadEmpId));
                }
                if (status != null && !status.isEmpty()) {
                        employeequery.addCriteria(
                                        Criteria.where("status").is(status));
                }
                // Exclude employees with dateOfJoining after the 15th of the current month
                employeequery.addCriteria(Criteria.where("sections.workingInformation.doj").lte(fifteenthDate));

                List<UserInfo> employeeDetails = mongoTemplate.find(employeequery,
                                UserInfo.class);
                // Extract empId
                List<String> empIdsUnderRepMan = new ArrayList<>();
                for (UserInfo detail : employeeDetails) {
                        empIdsUnderRepMan.add(detail.getEmpId());
                }

                Query query = new Query();
                if (rmStatus != Status.PENDING.label) {
                        query.addCriteria(Criteria.where("rmStatus").ne(rmStatus));

                }

                if (empId != null && !empId.isEmpty()) {
                        query.addCriteria(Criteria.where("empId").is(empId));
                } else {
                        query.addCriteria(Criteria.where("empId").in(empIdsUnderRepMan));
                }

                if ((fromDate != null && !fromDate.isEmpty()) && (toDate != null &&
                                !toDate.isEmpty())) {
                        query.addCriteria(Criteria.where("iiyDate").gte(from).lte(to));
                }

                return mongoTemplate.find(query, IIYDetails.class, getCollectionName(organizationCode));
        }

        default Optional<IIYDetails> findTopByOrderByIdDesc(String organizationCode, MongoTemplate mongoTemplate) {
                Query query = new Query();
                query.with(Sort.by(Sort.Direction.DESC, "id"));
                query.limit(1);
                IIYDetails result = mongoTemplate.findOne(query, IIYDetails.class,
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

        default BulkWriteResult bulkActivityInsert(MongoTemplate mongoTemplate, String orgId,
                        List<IIYDetails> dtoList) {
                BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                                IIYDetails.class, getCollectionName(orgId));
                return bulkOperations.insert(dtoList).execute();

        }
}
