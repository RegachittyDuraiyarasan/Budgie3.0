package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.AttendanceCitpl;

@Repository
public interface AttendanceCitplRepository extends MongoRepository<AttendanceCitpl, String> {

    @Query("{ 'attendanceDate' : { $gte: ?0, $lt: ?1 } }")
    List<AttendanceCitpl> findByAttendanceDate(LocalDate start, LocalDate end);

}
