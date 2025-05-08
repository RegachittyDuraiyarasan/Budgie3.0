package com.hepl.budgie.repository.leavemanagement;

import com.hepl.budgie.entity.payroll.PayrollLockMonth;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LockAttendanceRepository extends MongoRepository<PayrollLockMonth, String> {
	List<PayrollLockMonth> findByStatus(String label);

	PayrollLockMonth findFirstByStatus(Boolean label);

	default Optional<PayrollLockMonth> findByStatusOrgIdAndLockMonthTrue(MongoTemplate mongoTemplate, String status,
			String orgId) {
		Criteria criteria = new Criteria().andOperator(Criteria.where("status").is(status),
				Criteria.where("orgId").is(orgId), Criteria.where("payrollMonths.lockMonth").is(true));

		Query query = new Query(criteria);
		PayrollLockMonth result = mongoTemplate.findOne(query, PayrollLockMonth.class);

		return Optional.ofNullable(result);
	}

}
