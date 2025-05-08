package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.*;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PayrollLockMonthRepository extends MongoRepository<PayrollLockMonth, String> {
	static final String COLLECTION_NAME = "payroll_t_lock_current_month";

	default String getCollectionName(String country) {
		return country.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + "_" + country);
	}

	default void saveAttendanceDate(AttendanceDateDTO request, MongoTemplate mongoTemplate, String country) {
		mongoTemplate.save(request, getCollectionName(country));
	}

	default PayrollMonth findPayrollMonthByMonthYear(String monthYear, String org, MongoTemplate mongoTemplate,
			String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("orgId").is(org).and("status").is(Status.ACTIVE.label)
				.and("payrollMonths.payrollMonth").is(monthYear));

		query.fields().include("finYear").include("fromFinYear").include("toFinYear").include("payrollMonths.$");

		PayrollLockMonth payrollLockMonth = mongoTemplate.findOne(query, PayrollLockMonth.class,
				getCollectionName(country));

		if (payrollLockMonth == null || payrollLockMonth.getPayrollMonths().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Payroll month " + monthYear + " not found for organization " + org);
		}

		PayrollLockMonth.PayrollMonths foundMonth = payrollLockMonth.getPayrollMonths().get(0);

		return PayrollMonth.builder().finYear(payrollLockMonth.getFinYear())
				.fromFinYear(payrollLockMonth.getFromFinYear()).toFinYear(payrollLockMonth.getToFinYear())
				.payrollMonth(foundMonth.getPayrollMonth())
				.startDate(foundMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()))
				.endDate(foundMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()))
				.lockMonth(foundMonth.getLockMonth()).mail(foundMonth.getMail()).payslip(foundMonth.getPayslip())
				.build();
	}

	default void updateAttendanceDate(String year, String orgId, PayrollLockMonth request, MongoTemplate mongoTemplate,
			String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("finYear").is(year));
		query.addCriteria(Criteria.where("orgId").is(orgId));
		query.addCriteria(Criteria.where("status").is(Status.ACTIVE.label));

		Update update = new Update();
		update.set("payrollMonths", request.getPayrollMonths());

		mongoTemplate.findAndModify(query, update, PayrollLockMonth.class, getCollectionName(country));

	}

	default Optional<PayrollLockMonth> findByFinYearAndOrgId(String year, String orgId, MongoTemplate mongoTemplate,
			String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("finYear").is(year));
		query.addCriteria(Criteria.where("orgId").is(orgId));
		query.addCriteria(Criteria.where("status").is(Status.ACTIVE.label));
		return Optional.ofNullable(mongoTemplate.findOne(query, PayrollLockMonth.class, getCollectionName(country)));
	}

	default PayrollLockMonth savePayrollMonth(String finYear, String orgId, PayrollLockMonth request,
			MongoTemplate mongoTemplate, String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("finYear").is(finYear));
		query.addCriteria(Criteria.where("orgId").is(orgId));

		Update update = new Update().set("payrollMonths", request.getPayrollMonths());
		return mongoTemplate.findAndModify(query, update, PayrollLockMonth.class, getCollectionName(country));
	}

	default PayrollLockMonth getLockedPayrollMonths(MongoTemplate mongoTemplate, String orgId, String country) {
		String collection = getCollectionName(country);
		Query query = new Query();
		query.addCriteria(Criteria.where("orgId").is(orgId).and("status").is(Status.ACTIVE.label));

		return mongoTemplate.findOne(query, PayrollLockMonth.class, collection);
	}

	default PayrollMonth getLockedPayrollMonth(MongoTemplate mongoTemplate, String orgId, String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("orgId").is(orgId).and("status").is(Status.ACTIVE.label)
				.and("payrollMonths.lockMonth").is(true));

		query.fields().include("finYear").include("fromFinYear").include("toFinYear").include("payrollMonths.$");

		PayrollLockMonth payrollLockMonth = mongoTemplate.findOne(query, PayrollLockMonth.class,
				getCollectionName(country));

		if (payrollLockMonth == null || payrollLockMonth.getPayrollMonths().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Locked payroll month not found");
		}

		PayrollLockMonth.PayrollMonths lockedMonth = payrollLockMonth.getPayrollMonths().stream().findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Locked payroll month not found"));

		return PayrollMonth.builder().finYear(payrollLockMonth.getFinYear())
				.fromFinYear(payrollLockMonth.getFromFinYear()).toFinYear(payrollLockMonth.getToFinYear())
				.payrollMonth(lockedMonth.getPayrollMonth())
				.startDate(lockedMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()))
				.endDate(lockedMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()))
				.lockMonth(lockedMonth.getLockMonth()).mail(lockedMonth.getMail()).payslip(lockedMonth.getPayslip())
				.build();
	}

	default Optional<PayrollLockMonth> getAttendanceDate(String finYear, MongoTemplate mongoTemplate, String orgId,
			String country) {
		Query query = new Query();
		query.addCriteria(Criteria.where("finYear").is(finYear).and("orgId").is(orgId));

		return Optional.ofNullable(mongoTemplate.findOne(query, PayrollLockMonth.class, getCollectionName(country)));
	}

	default List<PayrollMonthDTO> getPayrollMonths(String finYear, MongoTemplate mongoTemplate, String orgId,
			String country) {
		Criteria criteria = Criteria.where("finYear").is(finYear).and("orgId").is(orgId).and("status")
				.is(Status.ACTIVE.label);

		Aggregation aggregation = Aggregation.newAggregation(Aggregation.unwind("payrollMonths"),
				Aggregation.match(criteria),
				Aggregation.project().andExpression("payrollMonths.startDate").as("startDate")
						.andExpression("payrollMonths.endDate").as("endDate")
						.andExpression("payrollMonths.payrollMonth").as("payrollMonth")
						.andExpression("payrollMonths.lockMonth").as("lockMonth").andExpression("payrollMonths.payslip")
						.as("payslip").andExpression("payrollMonths.mail").as("mail"));

		return mongoTemplate.aggregate(aggregation, getCollectionName(country), PayrollMonthDTO.class)
				.getMappedResults();
	}

	default List<String> getActivePayrollMonths(MongoTemplate mongoTemplate, String orgId, String country) {
		Criteria criteria = Criteria.where("orgId").is(orgId).and("status").is(Status.ACTIVE.label);

		Aggregation aggregation = Aggregation.newAggregation(Aggregation.unwind("payrollMonths"),
				Aggregation.match(criteria),
				Aggregation.project().andExclude("_id").andExpression("payrollMonths.payrollMonth").as("payrollMonth")

		);
		List<Document> results = mongoTemplate.aggregate(aggregation, getCollectionName(country), Document.class)
				.getMappedResults();

		return results.stream().map(doc -> doc.getString("payrollMonth")).collect(Collectors.toList());

	}

	default PayrollLockMonth getPayrollByFinYear(String finYear, MongoTemplate mongoTemplate, String orgId,
			String country) {
		Query query = new Query();
		String collection = getCollectionName(country);
		query.addCriteria(Criteria.where("finYear").is(finYear).and("orgId").is(orgId));

		return mongoTemplate.findOne(query, PayrollLockMonth.class, collection);
	}

	default List<PayrollLockMonth> getPayrollByOrgAndStatus(MongoTemplate mongoTemplate, String org, String country,
			String status) {
		String collection = getCollectionName(country);
		Query query = new Query();
		query.addCriteria(Criteria.where("orgId").is(org).and("status").is(status));
		return mongoTemplate.find(query, PayrollLockMonth.class, getCollectionName(country));
	}

	default Optional<PayrollLockMonth> findById(String id, String org, MongoTemplate mongoTemplate, String country) {
		Query query = new Query(
				Criteria.where("_id").is(id).and("orgId").is(org).and("status").is(Status.ACTIVE.label));
		return Optional.ofNullable(mongoTemplate.findOne(query, PayrollLockMonth.class, getCollectionName(country)));
	}
}
