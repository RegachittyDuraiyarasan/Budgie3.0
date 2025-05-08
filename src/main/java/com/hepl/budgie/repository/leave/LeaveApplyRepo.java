package com.hepl.budgie.repository.leave;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.leave.LeaveApply;

@Repository
public interface LeaveApplyRepo extends MongoRepository<LeaveApply, String> {

	public static final String COLLECTION_NAME = "leave_apply";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<LeaveApply> findLeavesWithDatesInRange(String startDate, String endDate, String org,
			MongoTemplate mongoTemplate) {

		Criteria leaveApplyDatesCriteria = Criteria.where("leaveApply.date").gte(startDate).lte(endDate);

		Criteria dateRangeCriteria = new Criteria().orOperator(Criteria.where("fromDate").gte(startDate).lte(endDate),

				Criteria.where("toDate").gte(startDate).lte(endDate), new Criteria()
						.andOperator(Criteria.where("fromDate").lte(startDate), Criteria.where("toDate").gte(endDate)));

		Query query = new Query(new Criteria().orOperator(leaveApplyDatesCriteria, dateRangeCriteria));

		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatus(String empId, String leaveType,
			String leaveCategory, String status, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveType").is(leaveType).and("leaveCategory")
				.is(leaveCategory).and("status").is(status));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpId(String empId, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndLeaveApplyDate(String empId, String date, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveApply.date").is(date));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default int countByEmpIdAndLeaveTypeAndDateListBetween(String empId, String leaveType, String startDate,
			String endDate, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveType").is(leaveType).and("dateList")
				.elemMatch(Criteria.where("$gte").is(startDate).and("$lte").is(endDate)));
		return (int) mongoTemplate.count(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(String empId,
			String leaveType, String leaveCategory, String status, String startDate, String endDate, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveType").is(leaveType).and("leaveCategory")
				.is(leaveCategory).and("status").is(status).and("dateList")
				.elemMatch(Criteria.where("$gte").is(startDate).and("$lte").is(endDate)));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndLeaveCancelAndFromToDateListBetween(
			String empId, String leaveType, String leaveCategory, String status, String leaveCancel, String startDate,
			String endDate, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveType").is(leaveType).and("leaveCategory")
				.is(leaveCategory).and("status").is(status).and("leaveCancel").is(leaveCancel).and("dateList")
				.elemMatch(Criteria.where("$gte").is(startDate).and("$lte").is(endDate)));
		return mongoTemplate.find(query, LeaveApply.class, getCollectionName(org));
	}

	default List<LeaveApply> findByEmpIdAndFromDateAndToDateBetween(String empId, String org,
			LocalDate payrollStartDate, LocalDate payrollEndDate, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(org);
		String payrollStartStr = payrollStartDate.toString();
		String payrollEndStr = payrollEndDate.toString();
		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
				Criteria.where("fromDate").gte(payrollStartStr), Criteria.where("toDate").lte(payrollEndStr)));
		return mongoTemplate.find(query, LeaveApply.class, collection);

	}

	default List<LeaveApply> findByEmpIdAndStatusAndLeaveCategoryAndLeaveCancelWithDateRange(String empId,
			String status, String leaveCategory, String leaveCancel, ZonedDateTime startDate, ZonedDateTime endDate,
			String orgId, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(orgId);
		String startDateStr = startDate.toLocalDate().toString();
		String endDateStr = endDate.toLocalDate().toString();

		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
				Criteria.where("status").is(status), Criteria.where("leaveCategory").is(leaveCategory),
				Criteria.where("leaveCancel").is(leaveCancel),

				new Criteria().orOperator(Criteria.where("fromDate").gte(startDateStr).lte(endDateStr),
						Criteria.where("toDate").gte(startDateStr).lte(endDateStr),
						new Criteria().andOperator(Criteria.where("fromDate").lte(startDateStr),
								Criteria.where("toDate").gte(endDateStr)))));

		return mongoTemplate.find(query, LeaveApply.class, collection);
	}

	default LeaveApply findByLeaveCodeAndEmpIdAndStatusAndLeaveCategoryAndLeaveCancel(String leaveCode, String empId,
			String status, String category, String leaveCancel, String org, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(org);

		Query query = new Query(
				new Criteria().andOperator(Criteria.where("empId").is(empId), Criteria.where("status").is(status),
						Criteria.where("leaveCategory").is(category), Criteria.where("leaveCancel").is(leaveCancel)));

		return mongoTemplate.findOne(query, LeaveApply.class, collection);
	}

	default Optional<LeaveApply> findTopByOrderByLeaveCodeDesc(String orgId, MongoTemplate mongoTemplate) {
		String collection = getCollectionName(orgId);

		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "leaveCode")).limit(1);

		LeaveApply result = mongoTemplate.findOne(query, LeaveApply.class, collection);
		return Optional.ofNullable(result);
	}

	default void saveLeaveApply(LeaveApply leaveApply, String orgId, MongoTemplate mongoTemplate) {
		String collection = getCollectionName(orgId);
		mongoTemplate.save(leaveApply, collection);
	}

	default List<LeaveApply> findByEmpIdsAndFromDateAndToDateBetween(List<String> empIds, String orgId,
			LocalDate payrollStartDate, LocalDate payrollEndDate, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(orgId);
		String payrollStartStr = payrollStartDate.toString();
		String payrollEndStr = payrollEndDate.toString();
		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").in(empIds),
				Criteria.where("fromDate").gte(payrollStartStr), Criteria.where("toDate").lte(payrollEndStr)));
		return mongoTemplate.find(query, LeaveApply.class, collection);
	}

	default List<LeaveApply> findByEmpIdAndYearAndLeaveTypes(String empId, String year, List<String> leaveTypes,
			String status, String leaveCancel, String org, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(org);

		String startDate = year + "-01-01";
		String endDate = year + "-12-31";

		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
				new Criteria().orOperator(Criteria.where("fromDate").gte(startDate).lte(endDate),
						Criteria.where("toDate").gte(startDate).lte(endDate),
						new Criteria().andOperator(Criteria.where("fromDate").lte(startDate),
								Criteria.where("toDate").gte(endDate))),
				Criteria.where("leaveType").in(leaveTypes), Criteria.where("status").is(status),
				Criteria.where("leaveCancel").is(leaveCancel)));

		return mongoTemplate.find(query, LeaveApply.class, collection);
	}
	
	default List<LeaveApply> findByEmpIdAndYearAndLeaveType(String empId, String year, String leaveType,
			String status, String leaveCancel, String org, MongoTemplate mongoTemplate) {

		String collection = getCollectionName(org);

		String startDate = year + "-01-01";
		String endDate = year + "-12-31";

		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
				new Criteria().orOperator(Criteria.where("fromDate").gte(startDate).lte(endDate),
						Criteria.where("toDate").gte(startDate).lte(endDate),
						new Criteria().andOperator(Criteria.where("fromDate").lte(startDate),
								Criteria.where("toDate").gte(endDate))),
				Criteria.where("leaveType").is(leaveType), Criteria.where("status").is(status),
				Criteria.where("leaveCancel").is(leaveCancel)));

		return mongoTemplate.find(query, LeaveApply.class, collection);
	}

	default List<LeaveApply> findByEmpIdAndLeaveType(String empId, String leaveType,String status, String org, MongoTemplate mongoTemplate) {
		
		Query query = new Query(Criteria.where("empId").is(empId).and("leaveType").is(leaveType).and("status").ne(status));
		return mongoTemplate.find(query, LeaveApply.class,getCollectionName(org));
	}
}
