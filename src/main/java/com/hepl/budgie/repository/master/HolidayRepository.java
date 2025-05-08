package com.hepl.budgie.repository.master;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.settings.Holiday;

public interface HolidayRepository extends MongoRepository<Holiday, String> {
	public static final String COLLECTION_NAME = "m_holidays";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<Holiday> findByDateBetween(String orgId, LocalDate startDate, LocalDate endDate,
			MongoTemplate mongoTemplate) {

		String collectionName = getCollectionName(orgId);
		Query query = new Query();
		query.addCriteria(Criteria.where("date").gte(startDate).lte(endDate));
		return mongoTemplate.find(query, Holiday.class, collectionName);
	}

	default Optional<Holiday> findByDateRange(LocalDateTime startOfDay, LocalDateTime startOfNextDay, String org,
			MongoTemplate mongoTemplate) {

		String collectionName = getCollectionName(org);
		Query query = new Query(Criteria.where("date").gte(startOfDay).lt(startOfNextDay));

		Holiday result = mongoTemplate.findOne(query, Holiday.class, collectionName);
		return Optional.ofNullable(result);
	}
}
