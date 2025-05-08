package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollPtDTO;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.PayrollPt;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoTransactionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollPTRepository extends MongoRepository<PayrollPt, String> {

    final String COLLECTION_NAME = "payroll_m_pt_";
    default Optional<PayrollPt> findLatestComponent(String org, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollPt.class, COLLECTION_NAME + org));
    }

    default void addPT(String orgId, PayrollPtDTO request, MongoTemplate mongoTemplate) {
        mongoTemplate.save(request, COLLECTION_NAME + orgId);
    }

    default Optional<PayrollPt> existByState(String orgId, PayrollPtDTO request, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("state").is(request.getState()));
//        query.addCriteria(Criteria.where("periodicity").is(request.getPeriodicity()));
//        query.addCriteria(Criteria.where("deductionType").is(request.getDeductionType()));

        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollPt.class, COLLECTION_NAME + orgId));
    }

    default boolean checkRangeDetails(String orgId, PayrollPtDTO.RangeDetails rangeDetails, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("rangeDetails").elemMatch(
                Criteria.where("gender").is(rangeDetails.getGender())
                        .and("salaryFrom").lte(rangeDetails.getSalaryFrom()) // Check if rangeDetails.getSalaryFrom is within range
                        .andOperator(
                                new Criteria().orOperator(
                                        Criteria.where("salaryTo").gte(rangeDetails.getSalaryTo()), // Check if salaryTo is greater than or equal
                                        Criteria.where("salaryTo").is(0) // Allow salaryTo to be 0
                                )
                        )
                        .and("taxAmount").is(rangeDetails.getTaxAmount())    // Exact match for taxAmount
        ));

        return mongoTemplate.exists(query, PayrollPt.class, COLLECTION_NAME + orgId);
    }

    default List<PayrollPt> getAllPTRecords(String orgId, MongoTemplate mongoTemplate) {
        return mongoTemplate.findAll(PayrollPt.class, COLLECTION_NAME + orgId);
    }
}
