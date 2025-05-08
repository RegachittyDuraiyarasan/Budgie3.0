package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.entity.attendancemanagement.AttendanceData;
import com.hepl.budgie.entity.attendancemanagement.AttendancePunchInformation;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

@Repository
public interface AttendancePunchInformationRepository extends MongoRepository<AttendancePunchInformation, String> {

    public static final String COLLECTION_NAME = "attendance_punch_information";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default void savePunchAttendanceData(MongoTemplate mongoTemplate, String organization,
            Collection<AttendancePunchInformation> punchData, String updatedBy) {

        if (organization == null || organization.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ORGANIZATION_DETAILS_NOT_FOUND);
        }
        String collectionName = getCollectionName(organization);
        for (AttendancePunchInformation punch : punchData) {
            Query query = new Query();
            query.addCriteria(Criteria.where("empId").is(punch.getEmpId())
                    .and("monthYear").is(punch.getMonthYear()));

            boolean documentExists = mongoTemplate.exists(query, collectionName);
            if (!documentExists) {
                for (AttendanceData newData : punch.getAttendanceData()) {
                    newData.setUpdatedBy(updatedBy);
                    newData.setUpdatedAt(LocalDate.now());
                }
                mongoTemplate.insert(punch, collectionName);
            } else {
                for (AttendanceData newData : punch.getAttendanceData()) {
                    newData.setUpdatedBy(updatedBy);
                    newData.setUpdatedAt(LocalDate.now());
                    Query attendanceQuery = new Query()
                            .addCriteria(Criteria.where("empId").is(punch.getEmpId())
                                    .and("monthYear").is(punch.getMonthYear())
                                    .and("attendanceData").elemMatch(Criteria.where("date").is(newData.getDate())));

                    Update replaceUpdate = new Update().set("attendanceData.$", newData);
                    UpdateResult result = mongoTemplate.updateFirst(attendanceQuery, replaceUpdate, collectionName);
                    if (result.getMatchedCount() == 0) {
                        Update pushUpdate = new Update().push("attendanceData", newData);
                        mongoTemplate.updateFirst(query, pushUpdate, collectionName);
                    }
                }
                Update updateTimestamp = new Update().set("updatedAt", LocalDateTime.now());
                mongoTemplate.updateFirst(query, updateTimestamp, collectionName);
            }
        }
    }

    Optional<AttendancePunchInformation> findByEmpId(String empId);

    default AttendancePunchInformation findByEmpIdAndMonthYear(String empId, String monthyear, String orgId,
            MongoTemplate mongoTemplate) {

        String collectionName = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("monthYear").is(monthyear));
        return mongoTemplate.findOne(query, AttendancePunchInformation.class, collectionName);
    }
}
