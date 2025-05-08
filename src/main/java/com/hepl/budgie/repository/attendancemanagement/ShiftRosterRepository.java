package com.hepl.budgie.repository.attendancemanagement;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.DateTimeException;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.RosterDetails;
import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.mongodb.bulk.BulkWriteResult;

@Repository
public interface ShiftRosterRepository extends MongoRepository<ShiftRoster, String> {

    public static final String COLLECTION_NAME = "attendance_shift_roster";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default void saveShiftRosterBulk(List<ShiftRoster> recordsToUpdate, String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);

        for (ShiftRoster roster : recordsToUpdate) {
            Query query = new Query();
            query.addCriteria(Criteria.where("empId").is(roster.getEmpId()).and("monthYear").is(roster.getMonthYear()));

            Update update = new Update();
            update.set("rosterDetails", roster.getRosterDetails());
            update.set("updatedDate", LocalDateTime.now());
            update.set("updatedBy", orgId);

            bulkOps.upsert(query, update);
        }

        bulkOps.execute();
    }

    default ShiftRoster findByMonthYearAndEmpId(String monthYear, String empId, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
        return mongoTemplate.findOne(query, ShiftRoster.class, collection);
    }

    default BulkWriteResult shiftRosterBulkUpsert(MongoTemplate mongoTemplate, String orgId,
            List<Map<String, Object>> validRows) {

        String collection = getCollectionName(orgId);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ShiftRoster.class,
                collection);

        Map<String, Integer> monthMap = Map.ofEntries(
                Map.entry("January", 1),
                Map.entry("February", 2),
                Map.entry("March", 3),
                Map.entry("April", 4),
                Map.entry("May", 5),
                Map.entry("June", 6),
                Map.entry("July", 7),
                Map.entry("August", 8),
                Map.entry("September", 9),
                Map.entry("October", 10),
                Map.entry("November", 11),
                Map.entry("December", 12));

        for (Map<String, Object> row : validRows) {
            String empId = (String) row.get("Employee_ID");
            String monthName = (String) row.get("Month");
            String yearStr = String.valueOf(row.get("Year"));

            if (empId == null || monthName == null || yearStr == null)
                continue;

            Integer month = monthMap.getOrDefault(monthName, -1);
            if (month == -1)
                continue;

            int year = Integer.parseInt(yearStr);
            YearMonth ym = YearMonth.of(year, month);
            String monthYear = String.format("%02d-%d", month, year);

            List<RosterDetails> rosterDetails = new ArrayList<>();
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                String dayStr = String.valueOf(day);
                Object shiftObj = row.get(dayStr);
                if (shiftObj != null && !shiftObj.toString().trim().isEmpty()) {
                    try {
                        RosterDetails detail = new RosterDetails();
                        detail.setDate(LocalDate.of(year, month, day));
                        detail.setShift(shiftObj.toString().trim());
                        rosterDetails.add(detail);
                    } catch (DateTimeException e) {
                    }
                }
            }

            Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));

            Update update = new Update()
                    .set("rosterDetails", rosterDetails)
                    .set("updatedDate", LocalDateTime.now())
                    .set("updatedBy", orgId);

            bulkOps.upsert(query, update);
        }
        return bulkOps.execute();
    }

    default List<ShiftRoster> finfByEmpIdAndMonthYear(String orgId, String monthYear, String empId, List<UserInfo> users, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("empId").is(empId));
        }
    
        if (monthYear != null && !monthYear.isEmpty()) {
            query.addCriteria(Criteria.where("monthYear").is(monthYear));
        }
    
        return mongoTemplate.find(query, ShiftRoster.class, collection);
    }
}
