package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationCitpl;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

@Repository
public interface AttendanceInformationCitplRepository extends MongoRepository<AttendanceInformationCitpl, String> {

    public static final String COLLECTION_NAME = "attendance_information";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default void saveAttendanceCitplData(MongoTemplate mongoTemplate, String organization,
            Collection<AttendanceInformationCitpl> attendanceData) {

        if (organization == null || organization.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ORGANIZATION_DETAILS_NOT_FOUND);
        }

        String collectionName = getCollectionName(organization);

        for (AttendanceInformationCitpl attendance : attendanceData) {
            Query query = new Query();
            query.addCriteria(Criteria.where("empId").is(attendance.getEmpId())
                    .and("monthYear").is(attendance.getMonthYear()));

            Update update = new Update();

            for (AttendanceInfo newInfo : attendance.getAttendanceInfo()) {
                Query attendanceQuery = new Query()
                        .addCriteria(Criteria.where("empId").is(attendance.getEmpId()))
                        .addCriteria(Criteria.where("monthYear").is(attendance.getMonthYear()))
                        .addCriteria(Criteria.where("attendanceInfo.attendanceDate").is(newInfo.getAttendanceDate()));

                Update updateExisting = new Update().set("attendanceInfo.$", newInfo);
                UpdateResult result = mongoTemplate.updateFirst(attendanceQuery, updateExisting, collectionName);

                if (result.getModifiedCount() == 0) {
                    update.push("attendanceInfo", newInfo);
                }
            }

            update.set("updatedAt", LocalDateTime.now());
            update.setOnInsert("empId", attendance.getEmpId());
            update.setOnInsert("monthYear", attendance.getMonthYear());
            update.setOnInsert("attendanceInfo", new ArrayList<>(attendance.getAttendanceInfo()));

            mongoTemplate.upsert(query, update, collectionName);
        }
    }

    List<AttendanceInformationCitpl> findByMonthYear(String format);

}
