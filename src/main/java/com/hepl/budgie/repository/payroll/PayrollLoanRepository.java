package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.PayrollLoan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollLoanRepository extends MongoRepository<PayrollLoan, String> {
    final String COLLECTION_NAME = "payroll_t_emp_loans_";
    default Optional<PayrollLoan> findLatestComponent(String orgId, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollLoan.class, COLLECTION_NAME + orgId));
    }
    default boolean findByEmpIdAndBalanceisZero(String empId, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("balanceAmount").is(0));
        return mongoTemplate.exists(query, COLLECTION_NAME + orgId);
    }

    default void saveLoan(PayrollLoan payrollLoan, MongoTemplate mongoTemplate, String orgId) {
        mongoTemplate.save(payrollLoan, COLLECTION_NAME + orgId);
    }

    default List<PayrollLoan> findByLoans(MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("balanceAmount").is(0));
        return mongoTemplate.find(query, PayrollLoan.class, COLLECTION_NAME + orgId);
    }
}
