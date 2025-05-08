package com.hepl.budgie.repository.attendancemanagement;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.DayType;
import java.util.List;


@Repository
public interface DayTypeRepository extends MongoRepository<DayType, String>{

	List<DayType> findByStatusTrue();
}
