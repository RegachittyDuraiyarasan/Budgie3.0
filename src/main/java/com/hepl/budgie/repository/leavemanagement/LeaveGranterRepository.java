package com.hepl.budgie.repository.leavemanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.leavemanagement.LeaveGranter;

public interface LeaveGranterRepository extends MongoRepository<LeaveGranter, String> {

	public static final String COLLECTION_NAME = "leave_granter";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<LeaveGranter> findByProcessedType(String processedType, String organizationCode,
			MongoTemplate mongoTemplate) {

		Query query = new Query(Criteria.where("processedType").is(processedType));
		return mongoTemplate.find(query, LeaveGranter.class, getCollectionName(organizationCode));
	}

	default LeaveGranter saveLeaveGranter(LeaveGranter leaveGranter, String organizationCode,
			MongoTemplate mongoTemplate) {
		return mongoTemplate.save(leaveGranter, getCollectionName(organizationCode));
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

}
