package com.hepl.budgie.repository.attendancemanagement;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;

@Repository
public interface AttendanceRegularizationRepository extends MongoRepository<AttendanceRegularization, String>{

    public static final String COLLECTION_NAME = "attendance_regularization";
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    AttendanceRegularization findByEmployeeId(String empId);

    AttendanceRegularization findTopByOrderByRegularizationCodeDesc();

    AttendanceRegularization findByEmployeeIdAndRegularizationCode(String empId, String regCode);

    
}
