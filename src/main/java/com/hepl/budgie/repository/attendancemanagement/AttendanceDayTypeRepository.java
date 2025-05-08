package com.hepl.budgie.repository.attendancemanagement;

import java.util.*;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.AttendanceDayType;

@Repository
public interface AttendanceDayTypeRepository extends MongoRepository<AttendanceDayType, String>{

    static final String COLLECTION_NAME = "attendance_day_type";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default AttendanceDayType findByIdAndOrg(String dayType, String orgId, MongoTemplate mongoTemplate){

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(new ObjectId(dayType)));
        return mongoTemplate.findOne(query, AttendanceDayType.class, collection);
    }

    default AttendanceDayType findByDayType(String dayType, String orgId, MongoTemplate mongoTemplate){

        String collection = getCollectionName(orgId);
        Query query = new Query();
        query.addCriteria(Criteria.where("dayType").is(dayType));
        return mongoTemplate.findOne(query, AttendanceDayType.class, collection);
    }

    default List<String> findAllDayTypes(MongoTemplate mongoTemplate, String orgId) {

        String collection = getCollectionName(orgId);
        List<AttendanceDayType> results = mongoTemplate.findAll( AttendanceDayType.class, collection);
        return results.stream().map(AttendanceDayType::getDayType).toList();
    }
}
