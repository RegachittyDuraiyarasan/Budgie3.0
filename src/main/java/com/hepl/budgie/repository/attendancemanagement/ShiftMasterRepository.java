package com.hepl.budgie.repository.attendancemanagement;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;

@Repository
public interface ShiftMasterRepository extends MongoRepository<ShiftMaster, String>{

	public static final String COLLECTION_NAME = "m_shift";
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

	List<ShiftMaster> findByStatusTrue();

    default ShiftMaster findByShiftName(String shift, String orgId, MongoTemplate mongoTemplate){

        Query query = new Query();
        String collection = getCollectionName(orgId);
        query.addCriteria(Criteria.where("shiftName").is(shift));
        return mongoTemplate.findOne(query, ShiftMaster.class, collection);
    }

    default List<String> findAllShiftCodes(MongoTemplate mongoTemplate, String orgId) {

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(true));
        query.fields().include("shiftCode"); 
        List<ShiftMaster> results = mongoTemplate.find(query, ShiftMaster.class, collection);
        return results.stream().map(ShiftMaster::getShiftCode).toList();
    }

    default ShiftMaster findByShiftCode(String shiftCode, String orgId, MongoTemplate mongoTemplate){

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("shiftCode").is(shiftCode));
        return mongoTemplate.findOne(query, ShiftMaster.class, collection);
    }
    
}
