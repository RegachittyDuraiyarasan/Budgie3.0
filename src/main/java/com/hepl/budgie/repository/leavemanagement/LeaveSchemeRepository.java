package com.hepl.budgie.repository.leavemanagement;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;
import com.mongodb.client.result.UpdateResult;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveSchemeRepository extends MongoRepository<LeaveScheme, String> {

	public static final String COLLECTION_NAME = "leave_scheme";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default UpdateResult deleteLeaveScheme(String id, MongoTemplate mongoTemplate, String organizationCode,
			String authUser) {
		Query query = new Query(Criteria.where("_id").is(id));
		Update update = new Update();
		update.set("status", Status.DELETED.label);
		update = auditInfo(update, false, authUser);
		return mongoTemplate.updateFirst(query, update, LeaveScheme.class, getCollectionName(organizationCode));
	}

	default List<LeaveScheme> findByActiveStatus(String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
		return mongoTemplate.find(query, LeaveScheme.class, getCollectionName(organizationCode));
	}

	default Optional<LeaveScheme> findBySchemeName(String schemeName, MongoTemplate mongoTemplate,
			String organizationCode) {
		Query query = new Query(Criteria.where("schemeName").is(schemeName).and("status").nin(Status.DELETED.label));
		LeaveScheme result = mongoTemplate.findOne(query, LeaveScheme.class, getCollectionName(organizationCode));
		return Optional.ofNullable(result);
	}

	default List<LeaveScheme> findByStatus(String status, MongoTemplate mongoTemplate, String organizationCode) {
		Query query = new Query(Criteria.where("status").is(status));
		return mongoTemplate.find(query, LeaveScheme.class, getCollectionName(organizationCode));
	}

	default String insertOrUpdate(LeaveScheme request, MongoTemplate mongoTemplate, String organizationCode,
			String authUser) {
		Query query = new Query(Criteria.where("schemeName").is(request.getSchemeName()));

		boolean isNew = !mongoTemplate.exists(query, LeaveScheme.class, getCollectionName(organizationCode));
		String collectionName = getCollectionName(organizationCode);

		Update update = new Update();
		update.set("schemeName", request.getSchemeName());
		update.set("applicableTo", request.getApplicableTo());
		update.set("probationType", request.getProbationType());
		update.set("periodicity", request.getPeriodicity());

		if (isNew) {
			update.setOnInsert("status", Status.ACTIVE.getLabel());
		} else {
			update.set("status", request.getStatus());
		}

		update = auditInfo(update, isNew, authUser);

		mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
				LeaveScheme.class, collectionName);

		String action = isNew ? "created" : "updated";
		String message = String.format("Leave scheme %s successfully %s", request.getSchemeName(), action);
		return message;
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
