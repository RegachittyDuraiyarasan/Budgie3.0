package com.hepl.budgie.repository.leavemanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.leavemanagement.LeaveMaster;

public interface LeaveMasterRepository extends MongoRepository<LeaveMaster, String> {

	public static final String COLLECTION_NAME = "leave_master";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<LeaveMaster> findAllByEmpId(String empId, String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId));
		return mongoTemplate.find(query, LeaveMaster.class, getCollectionName(organizationCode));
	}

	default Optional<LeaveMaster> findByEmpIdAndYearAndLeaveType(String empId, String year, String leaveType,
			String organizationCode, MongoTemplate mongoTemplate) {

		Query query = new Query(Criteria.where("empId").is(empId).and("year").is(year)
				.and("leaveTransactions.leaveTypeName").is(leaveType));

		LeaveMaster result = mongoTemplate.findOne(query, LeaveMaster.class, getCollectionName(organizationCode));

		return Optional.ofNullable(result);
	}

	default Optional<LeaveMaster> findByEmpIdYearAndTransactionDateRange(String empId, String year, String startDate,
			String endDate, MongoTemplate mongoTemplate, String organizationCode) {

		Criteria criteria = Criteria.where("empId").is(empId).and("year").is(year).and("leaveTransactions")
				.elemMatch(Criteria.where("fromDate").gte(startDate).and("toDate").lte(endDate));

		Query query = new Query(criteria);

		LeaveMaster result = mongoTemplate.findOne(query, LeaveMaster.class, getCollectionName(organizationCode));

		return Optional.ofNullable(result);
	}

	default Optional<LeaveMaster> findLeaveBalanceSummaryByEmpIdAndYearAndLeaveType(String empId, String year,
			String leaveType, String organizationCode, MongoTemplate mongoTemplate) {

		Query query = new Query(Criteria.where("empId").is(empId).and("year").is(year)
				.and("leaveBalanceSummary.leaveTypeName").is(leaveType));

		query.fields().include("leaveBalanceSummary.$");

		LeaveMaster result = mongoTemplate.findOne(query, LeaveMaster.class, getCollectionName(organizationCode));

		return Optional.ofNullable(result);
	}

	default Optional<LeaveMaster> findByEmpIdAndYear(String empId, String year, String organizationCode,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("year").is(year));
		LeaveMaster result = mongoTemplate.findOne(query, LeaveMaster.class, getCollectionName(organizationCode));
		return Optional.ofNullable(result);
	}

	default LeaveMaster saveOrUpdateLeaveMaster(LeaveMaster leaveMaster, MongoTemplate mongoTemplate,
			String organizationCode) {
		Query query = new Query(
				Criteria.where("empId").is(leaveMaster.getEmpId()).and("year").is(leaveMaster.getYear()));

		Update update = new Update();
		update.set("leaveBalanceSummary", leaveMaster.getLeaveBalanceSummary());
		update.set("leaveTransactions", leaveMaster.getLeaveTransactions());

		return mongoTemplate.findAndModify(query, update, LeaveMaster.class, getCollectionName(organizationCode));
	}

	default boolean exists(Query query, MongoTemplate mongoTemplate, String organizationCode) {
		return mongoTemplate.exists(query, LeaveMaster.class, getCollectionName(organizationCode));
	}

	default LeaveMaster saveLeaveMaster(LeaveMaster leaveMaster, String organizationCode, MongoTemplate mongoTemplate) {
		return mongoTemplate.save(leaveMaster, getCollectionName(organizationCode));
	}

	default List<LeaveMaster> findByEmpIdInAndOrganizationCode(List<String> empIds, String organizationCode,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").in(empIds).and("organizationCode").is(organizationCode));
		return mongoTemplate.find(query, LeaveMaster.class, getCollectionName(organizationCode));
	}
}
