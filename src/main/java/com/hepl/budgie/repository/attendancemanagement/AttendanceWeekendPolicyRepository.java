package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;

@Repository
public interface AttendanceWeekendPolicyRepository extends MongoRepository<AttendanceWeekendPolicy, String> {

	public static final String COLLECTION_NAME = "attendance_weekend_policy";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default Update auditInfo(Update update, boolean isNew, String authUser) {
		if (isNew) {
			update.setOnInsert("createdDate", LocalDateTime.now());
			update.setOnInsert("createdByUser", authUser);
		}
		update.set("lastModifiedDate", LocalDateTime.now());
		update.set("modifiedByUser", authUser);
		return update;
	}

	default AttendanceWeekendPolicy findByMonth(String month, String organization, MongoTemplate mongoTemplate) {

		String collectionName = getCollectionName(organization);
		Query query = new Query();
		query.addCriteria(Criteria.where("month").is(month));
		return mongoTemplate.findOne(query, AttendanceWeekendPolicy.class, collectionName);
	}

	default AttendanceWeekendPolicy findByMonthYear(String monthYear, String organization,
			MongoTemplate mongoTemplate) {

		String collectionName = getCollectionName(organization);
		Query query = new Query(Criteria.where("month").is(monthYear));
		return mongoTemplate.findOne(query, AttendanceWeekendPolicy.class, collectionName);
	}

	default List<AttendanceWeekendPolicy> findByMonths(String orgId, List<String> months, MongoTemplate mongoTemplate) {
		String collectionName = getCollectionName(orgId);
		Query query = new Query(Criteria.where("month").in(months) 
		);
	
		return mongoTemplate.find(query, AttendanceWeekendPolicy.class,collectionName);
	}
	
	
	
	default List<AttendanceWeekendPolicy> findByMonthYearBtw(String startMonthYear, String endMonthYear, String orgId,
			MongoTemplate mongoTemplate) {

		String collectionName = getCollectionName(orgId);
		Query query = new Query(Criteria.where("month").gte(startMonthYear).lte(endMonthYear));
		return mongoTemplate.find(query, AttendanceWeekendPolicy.class, collectionName);

	}
}
