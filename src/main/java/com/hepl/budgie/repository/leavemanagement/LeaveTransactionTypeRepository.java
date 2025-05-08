package com.hepl.budgie.repository.leavemanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;
import com.mongodb.client.result.UpdateResult;

@Repository
public interface LeaveTransactionTypeRepository extends MongoRepository<LeaveTransactionType, String> {

	public static final String COLLECTION_NAME = "leave_transaction_type";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<LeaveTransactionType> findByActiveStatus(String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
		return mongoTemplate.find(query, LeaveTransactionType.class, getCollectionName(organizationCode));
	}

	default UpdateResult deleteLeaveTransactionType(String id, String authUser, String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("_id").is(id));
		Update update = new Update();
		update.set("status", Status.DELETED.label);
		update = auditInfo(update, false, authUser);
		return mongoTemplate.updateFirst(query, update, LeaveTransactionType.class,
				getCollectionName(organizationCode));
	}

	default List<LeaveTransactionType> findByStatus(String status, String organizationCode,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("status").is(status));
		return mongoTemplate.find(query, LeaveTransactionType.class, getCollectionName(organizationCode));
	}

	default String insertOrUpdate(LeaveTransactionType request, String authUser, String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("leaveTransactionType").is(request.getLeaveTransactionType()));
		String collectionName = getCollectionName(organizationCode);

		boolean isNew = !mongoTemplate.exists(query, LeaveTransactionType.class, collectionName);

		Update update = new Update();
		update.set("leaveTransactionType", request.getLeaveTransactionType());
		update.set("allowFirstTime", request.getAllowFirstTime());
		update.set("balanceImpact", request.getBalanceImpact());
		update.set("directTransaction", request.getDirectTransaction());

		if (isNew) {
			update.setOnInsert("status", Status.ACTIVE.getLabel());
		} else {
			update.set("status", request.getStatus());
		}
		update = auditInfo(update, isNew, authUser);
		mongoTemplate.findAndModify(query, update, new FindAndModifyOptions().returnNew(true).upsert(true),
				LeaveTransactionType.class, collectionName);

		String action = isNew ? "created" : "updated";
		String message = String.format("Leave transaction type %s successfully %s", request.getLeaveTransactionType(),
				action);
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
