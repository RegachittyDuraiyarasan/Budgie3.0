package com.hepl.budgie.repository.leave;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.leave.LeaveApply;

@Repository
public interface LeaveApprovalRepository extends MongoRepository<LeaveApply, String> {

	public static final String COLLECTION_NAME = "leave_apply";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}
	
	default LeaveApply saveLeaveApproval(LeaveApply leaveApply, String org, MongoTemplate mongoTemplate) {
		return mongoTemplate.save(leaveApply, getCollectionName(org));
	}

	default List<LeaveApply> findByAppliedToAndStatus(String appliedTo, String status, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("appliedTo").is(appliedTo).and("status").is(status));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByAppliedToAndStatusIn(String appliedTo, List<String> statuses, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("appliedTo").is(appliedTo).and("status").in(statuses));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndStatus(String empId, String status, String orgId,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("status").is(status));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(orgId));
	}

	default List<LeaveApply> findByEmpIdAndStatusIn(String empId, List<String> statuses, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("status").in(statuses));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByAppliedToWithSort(String appliedTo, Pageable pageable, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("appliedTo").is(appliedTo)).with(pageable)
				.with(Sort.by(Sort.Direction.DESC, "id"));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndLeaveCategory(String empId, String leaveCategory, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveCategory").is(leaveCategory));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndDateRange(String empId, String fromDate, String toDate, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(
				Criteria.where("empId").is(empId).and("fromDate").gte(fromDate).and("toDate").lte(toDate));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndFileNames(String empId, List<String> fileNames, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("fileNames").in(fileNames));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default Optional<LeaveApply> findByLeaveCode(String leaveCode, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("leaveCode").is(leaveCode));
		LeaveApply result = mongoTemplate.findOne(query, LeaveApply.class, getCollectionName(org));
		return Optional.ofNullable(result);
	}
}
