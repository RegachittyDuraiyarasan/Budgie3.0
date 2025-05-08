package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.dto.attendancemanagement.AttendanceOverrideEntry;
import com.hepl.budgie.dto.attendancemanagement.AttendanceReportDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationHepl;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

@Repository
public interface AttendanceInformationRepository extends MongoRepository<AttendanceInformationHepl, String> {

    public static final String COLLECTION_NAME = "attendance_information";

    default String getCollectionNames(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default void saveAttendanceData(MongoTemplate mongoTemplate, String organization,
            Collection<AttendanceInformationHepl> attendanceData, String updatedBy) {
        if (organization == null || organization.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ORGANIZATION_DETAILS_NOT_FOUND);
        }
        String collectionName = getCollectionName(organization);
        for (AttendanceInformationHepl attendance : attendanceData) {
            Query query = new Query();
            query.addCriteria(Criteria.where("empId").is(attendance.getEmpId())
                    .and("monthYear").is(attendance.getMonthYear()));
            boolean documentExists = mongoTemplate.exists(query, collectionName);

            if (!documentExists) {
                mongoTemplate.insert(attendance, collectionName);
            } else {
                for (AttendanceInfo newInfo : attendance.getAttendanceInfo()) {
                    Query attendanceQuery = new Query()
                            .addCriteria(Criteria.where("empId").is(attendance.getEmpId())
                                    .and("monthYear").is(attendance.getMonthYear()))
                            .addCriteria(
                                    Criteria.where("attendanceInfo.attendanceDate").is(newInfo.getAttendanceDate()));

                    newInfo.setUpdatedBy(updatedBy);
                    newInfo.setUpdatedAt(LocalDate.now());

                    Update updateExisting = new Update().set("attendanceInfo.$", newInfo);
                    UpdateResult result = mongoTemplate.updateFirst(attendanceQuery, updateExisting, collectionName);

                    if (result.getMatchedCount() == 0) {
                        Update pushUpdate = new Update().push("attendanceInfo", newInfo);
                        mongoTemplate.updateFirst(query, pushUpdate, collectionName);
                    }
                }
                Update updateTimestamp = new Update().set("updatedAt", LocalDateTime.now());
                mongoTemplate.updateFirst(query, updateTimestamp, collectionName);
            }
        }
    }

    default AttendanceInformationHepl findByEmpIdAndMonthYear(String empId, String monthyear, String orgId,
            MongoTemplate mongoTemplate) {

        String collectionName = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(monthyear));
        return mongoTemplate.findOne(query, AttendanceInformationHepl.class, collectionName);
    }

    default void updateAttendance(String empId, String monthyear, String orgId,
                              MongoTemplate mongoTemplate, String attendanceData, String date) {

        String collectionName = getCollectionName(orgId);
        
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(monthyear));
        
        AttendanceInformationHepl result = mongoTemplate.findOne(query, AttendanceInformationHepl.class, collectionName);

        if (result != null) {
            boolean updated = false;
            
            for (AttendanceInfo info : result.getAttendanceInfo()) {
                if (info.getAttendanceDate().equals(date)) {
                    info.setAttendanceData(attendanceData);
                    info.setOverride(null); 
                    updated = true;
                    break;
                }
            }

            if (updated) {
                mongoTemplate.save(result, collectionName);
            }
        }
    }


    @org.springframework.data.mongodb.repository.Query(value = "{ 'empId': ?0, 'attendanceInfo.attendanceDate': { $in: ?1 } }")
    List<AttendanceInformationHepl> findByEmpIdAndDate(String empId, List<String> date);

    default List<AttendanceInformationHepl> findAttendanceRecords(MongoTemplate mongoTemplate, String empId,
            String startDate, String endDate, String orgId) {
        if (orgId == null || orgId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization ID is required.");
        }
        String collectionName = getCollectionName(orgId);
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("empId").is(empId),
                Criteria.where("attendanceInfo.attendanceDate").gte(startDate).lte(endDate));
        Query query = new Query(criteria);
        return mongoTemplate.find(query, AttendanceInformationHepl.class, collectionName);
    }

    List<AttendanceInformationHepl> findByEmpId(String empId);

    @org.springframework.data.mongodb.repository.Query(value = "{ 'empId': ?0, 'attendanceInfo.attendanceDate': { $gte: ?1, $lte: ?2 } }")
    List<AttendanceInformationHepl> findByEmpIdAndAttendanceDateBetween(String empId, String startDate, String endDate);

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

    Optional<AttendanceInformationHepl> findByEmpIdAndMonthYear(String empId, String yearMonth);

    default List<AttendanceReportDTO> getEmployeeAttendanceReport(MongoTemplate mongoTemplate, String orgId,
            String empId,
            LocalDate fromDate, LocalDate toDate, String empName) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String fromMonthYear = fromDate.format(formatter);
        String toMonthYear = toDate.format(formatter);
        String collection = getCollectionName(orgId);

        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId));
        query.addCriteria(Criteria.where("monthYear").gte(fromMonthYear).lte(toMonthYear));

        List<AttendanceInformationHepl> attendanceRecords = mongoTemplate.find(query, AttendanceInformationHepl.class,
                collection);
        List<AttendanceReportDTO> result = new ArrayList<>();

        for (AttendanceInformationHepl record : attendanceRecords) {
            for (AttendanceInfo details : record.getAttendanceInfo()) {
                LocalDate attendanceDate = LocalDate.parse(details.getAttendanceDate());
                if (!attendanceDate.isBefore(fromDate) && !attendanceDate.isAfter(toDate)) {
                    if (details.getInTime() != null && !details.getInTime().isEmpty()) {
                        result.add(new AttendanceReportDTO(empId, empName, attendanceDate, "IN", details.getInTime()));
                    }
                    if (details.getOutTime() != null && !details.getOutTime().isEmpty()) {
                        result.add(
                                new AttendanceReportDTO(empId, empName, attendanceDate, "OUT", details.getOutTime()));
                    }
                }
            }
        }
        result.sort(Comparator.comparing(AttendanceReportDTO::getDate));
        return result;
    }

    default void saveAttendance(MongoTemplate mongoTemplate, String orgId, String empId,
            String monthYear, AttendanceInfo attendanceInfo) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));

        Update update = new Update();
        update.setOnInsert("empId", empId);
        update.setOnInsert("monthYear", monthYear);
        update.push("attendanceInfo", attendanceInfo);
        mongoTemplate.upsert(query, update, AttendanceInformationHepl.class, collection);
    }

    default void updateAttendance(MongoTemplate mongoTemplate, String orgId, String empId, String monthYear,
            String todayDate, AttendanceInfo attendanceInfo) {
        String collection = getCollectionName(orgId);

        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId)
                .and("monthYear").is(monthYear)
                .and("attendanceInfo.attendanceDate").is(todayDate));

        Update update = new Update();
        update.set("attendanceInfo.$.inTime", attendanceInfo.getInTime());
        update.set("attendanceInfo.$.outTime", attendanceInfo.getOutTime());
        update.set("attendanceInfo.$.actualWorkHours", attendanceInfo.getActualWorkHours());
        update.set("attendanceInfo.$.shortfallHours", attendanceInfo.getShortFallHours());
        update.set("attendanceInfo.$.excessHours", attendanceInfo.getExcessHours());
        update.set("attendanceInfo.$.attendanceData", attendanceInfo.getAttendanceData());
        update.set("attendanceInfo.$.attendanceSchema", "Web Login");

        mongoTemplate.updateFirst(query, update, AttendanceInformationHepl.class, collection);
    }

    default List<AttendanceInformationHepl> findByempIdAndMonths(String empId, List<String> monthYear, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").in(monthYear));
        return mongoTemplate.find(query, AttendanceInformationHepl.class, collection);
    }

    default void updateOverride(String employeeId, String empId, String monthYear, List<AttendanceOverrideEntry> overrideList,
            String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        for (AttendanceOverrideEntry overrideEntry : overrideList) {
            String session1 = overrideEntry.getSession1();
            String session2 = overrideEntry.getSession2();
            String attendanceData;

            if ("P".equalsIgnoreCase(session1) && "P".equalsIgnoreCase(session2)) {
                attendanceData = "P";
            } else if ("A".equalsIgnoreCase(session1) && "A".equalsIgnoreCase(session2)) {
                attendanceData = "A";
            } else {
                attendanceData = session1 + ":" + session2;
            }
            boolean overrideFlag = true;
            Query query = new Query(Criteria.where("empId").is(empId)
                    .and("monthYear").is(monthYear)
                    .and("attendanceInfo.attendanceDate").is(overrideEntry.getDate()));

            Update updateExisting = new Update()
                    .set("attendanceInfo.$.attendanceData", attendanceData)
                    .set("attendanceInfo.$.override", "override")
                    .set("attendanceInfo.$.updatedBy", employeeId)
                    .set("attendanceInfo.$.updatedAt", LocalDate.now());
            UpdateResult updateResult = mongoTemplate.updateFirst(query, updateExisting,
                    AttendanceInformationHepl.class, collection);
            if (updateResult.getMatchedCount() == 0) {
                Query checkRecordQuery = new Query(Criteria.where("empId").is(empId)
                        .and("monthYear").is(monthYear));

                Update pushNewEntry = new Update()
                        .setOnInsert("empId", empId)
                        .setOnInsert("monthYear", monthYear)
                        .push("attendanceInfo", new AttendanceInfo(
                                overrideEntry.getDate(), null, null, null, null,
                                null, null, null, attendanceData, null, null, null, "override",null,null, empId, LocalDate.now()));
                mongoTemplate.upsert(checkRecordQuery, pushNewEntry, AttendanceInformationHepl.class, collection);
            }
        }
    }
}