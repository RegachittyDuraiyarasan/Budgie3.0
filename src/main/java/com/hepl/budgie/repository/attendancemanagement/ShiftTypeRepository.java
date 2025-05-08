package com.hepl.budgie.repository.attendancemanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.ShiftType;

@Repository
public interface ShiftTypeRepository extends MongoRepository<ShiftType, String>{

	public static final String COLLECTION_NAME = "shift_type";
	
	default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }
	
	Optional<ShiftType> findById(String id);
	
	List<ShiftType> findAllByStatusTrue();
}
