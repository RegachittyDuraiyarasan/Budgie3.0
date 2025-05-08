package com.hepl.budgie.repository.attendancemanagement;

import java.util.*;
import java.time.LocalDate;
import java.time.YearMonth;

import java.time.format.DateTimeFormatter;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.attendancemanagement.AttendanceDayTypeHistoryDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayType;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.entity.attendancemanagement.DayType;
import com.hepl.budgie.entity.attendancemanagement.DayTypeDetail;
import com.hepl.budgie.entity.attendancemanagement.UpdatedDayType;
import com.mongodb.bulk.BulkWriteResult;

@Repository
public interface AttendanceDayTypeHistoryRepository extends MongoRepository<AttendanceDayTypeHistory, String> {

    public static final String COLLECTION_NAME = "attendance_day_type_history";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default AttendanceDayTypeHistory saveAttendanceDayType(String orgId, AttendanceDayTypeHistoryDTO dayTypeHistoryDTO,
            AttendanceDayType dayType, MongoTemplate mongoTemplate) {
        String collectionName = getCollectionName(orgId);

        String empId = dayTypeHistoryDTO.getEmpId();
        String monthYear = dayTypeHistoryDTO.getDate().format(DateTimeFormatter.ofPattern("MM-yyyy"));
        LocalDate date = dayTypeHistoryDTO.getDate();
        Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
        AttendanceDayTypeHistory existingRecord = mongoTemplate.findOne(query, AttendanceDayTypeHistory.class,
                collectionName);

        DayTypeDetail newDayTypeDetail = new DayTypeDetail();
        newDayTypeDetail.setDayTypeId(dayType.getDayTypeId());
        newDayTypeDetail.setDayType(dayType.getDayType());
        newDayTypeDetail.setShiftCode(dayTypeHistoryDTO.getShift());

        UpdatedDayType newUpdatedDayType = new UpdatedDayType(date, newDayTypeDetail);

        if (existingRecord == null) {
            AttendanceDayTypeHistory newRecord = new AttendanceDayTypeHistory();
            newRecord.setEmpId(empId);
            newRecord.setMonthYear(monthYear);
            newRecord.setUpdatedDayType(new ArrayList<>());
            newRecord.getUpdatedDayType().add(newUpdatedDayType);

            return mongoTemplate.save(newRecord, collectionName);
        } else {
            List<UpdatedDayType> updatedDayTypes = existingRecord.getUpdatedDayType();
            Optional<UpdatedDayType> existingDayType = updatedDayTypes.stream()
                    .filter(dt -> dt.getDate().equals(date))
                    .findFirst();

            if (existingDayType.isPresent()) {
                existingDayType.get().setDayTypes(newDayTypeDetail);
            } else {
                updatedDayTypes.add(newUpdatedDayType);
            }
            return mongoTemplate.save(existingRecord, collectionName);
        }
    }

    default List<AttendanceDayTypeHistory> findByEmpId(String empId, String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId));
        return mongoTemplate.find(query, AttendanceDayTypeHistory.class, collection);
    }

    default AttendanceDayTypeHistory findByEmpIdAndMonthYear(String empId, String currentMonth, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(currentMonth));
        return mongoTemplate.findOne(query, AttendanceDayTypeHistory.class, collection);
    }

    default void SaveDayTypeHistory(MongoTemplate mongoTemplate, List<AttendanceDayTypeHistory> recordsToUpdate,
            String orgId) {

        String collection = getCollectionName(orgId);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collection);
        for (AttendanceDayTypeHistory history : recordsToUpdate) {
            Query query = new Query();
            query.addCriteria(
                    Criteria.where("empId").is(history.getEmpId()).and("monthYear").is(history.getMonthYear()));
            Update update = new Update();
            update.set("updatedDayType", history.getUpdatedDayType());

            bulkOps.upsert(query, update);
        }
        bulkOps.execute();
    }

    default BulkWriteResult dayTypeBulkUpsert(MongoTemplate mongoTemplate, String orgId,
            List<Map<String, Object>> validRows) {

        String collection = getCollectionName(orgId);
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                AttendanceDayTypeHistory.class, collection);

        for (Map<String, Object> row : validRows) {
            String empId = String.valueOf(row.get("Employee_ID")).trim();
            LocalDate date = LocalDate.parse(String.valueOf(row.get("Date")),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String shiftCode = row.get("Shift_Code") != null ? String.valueOf(row.get("Shift_Code")).trim() : null;
            String dayTypeStr = String.valueOf(row.get("Day_Type")).trim();

            String monthYear = YearMonth.from(date).format(DateTimeFormatter.ofPattern("MM-yyyy"));

            Query dayTypeQuery = new Query(Criteria.where("dayType").is(dayTypeStr));
            DayType dayTypeEntity = mongoTemplate.findOne(dayTypeQuery, DayType.class, "m_attendance_day_type_"+ orgId);
            if (dayTypeEntity == null) {
                continue;
            }

            DayTypeDetail dayTypeDetail = new DayTypeDetail();
            dayTypeDetail.setDayType(dayTypeEntity.getDayType());
            dayTypeDetail.setDayTypeId(dayTypeEntity.getId());
            dayTypeDetail.setShiftCode(shiftCode);

            UpdatedDayType updatedDayType = new UpdatedDayType();
            updatedDayType.setDate(date);
            updatedDayType.setDayTypes(dayTypeDetail);

            Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
            Update update = new Update().addToSet("updatedDayType", updatedDayType);
            bulkOps.upsert(query, update);
        }

        return bulkOps.execute();

    }
}
