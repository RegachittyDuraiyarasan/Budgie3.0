package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPRange;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollFBPRangeRepository extends MongoRepository<PayrollFBPRange, String> {
    static final String COLLECTION_NAME = "payroll_m_fbp_range_details";

    default Optional<PayrollFBPRange> findLatestComponent(String orgId, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPRange.class, COLLECTION_NAME));
    }

    default boolean existsByRangeIdActive(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("rangeId").is(id))
                .addCriteria(Criteria.where("orgId").is(orgId))
                .addCriteria(Criteria.where("status").is(Status.ACTIVE.label));
        return mongoTemplate.exists(query, PayrollFBPRange.class, COLLECTION_NAME);
    }
    default Optional<PayrollFBPRange> findByComponentId(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("rangeId").is(id))
                .addCriteria(Criteria.where("orgId").is(orgId));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPRange.class, COLLECTION_NAME));
    }

    default boolean updateStatus(String componentId, String newStatus, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("rangeId").is(componentId));
        Update update = new Update().set("status", newStatus);

        UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollFBPComponentMaster.class, COLLECTION_NAME);
        return result.getModifiedCount() > 0;
    }

    default boolean existsByRange(String type, MongoTemplate mongoTemplate, PayrollFBPRange request, String orgId) {
        Query query = new Query();
        query.addCriteria(
                new Criteria().orOperator(
                        Criteria.where("from").lte(request.getTo()).and("to").gte(request.getFrom()),
                        Criteria.where("to").is(0).and("from").lte(request.getFrom())
                ).and("status").nin(Status.DELETED.label).and("orgId").is(orgId)
        );
        if(type.equalsIgnoreCase("update"))
            query.addCriteria(Criteria.where("rangeId").nin(request.getRangeId()));
        return mongoTemplate.exists(query, PayrollFBPRange.class, COLLECTION_NAME);
    }


    default boolean upsert(PayrollFBPRange request, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("rangeId").is(request.getRangeId()));
        Update update = buildUpdateFromDTO(request);
        UpdateResult result = mongoTemplate.upsert(query, update, PayrollFBPRange.class, COLLECTION_NAME);
        return result.wasAcknowledged();
    }

    static Update buildUpdateFromDTO(PayrollFBPRange request) {
        return new Update()
                .set("from", request.getFrom())
                .set("to", request.getTo())
                .set("status", request.getStatus())
                .set("orgId", request.getOrgId())
                .set("_class", PayrollFBPRange.class.getName());
    }

    default List<PayrollFBPRange> findByNonDeleteList(MongoTemplate mongoTemplate, String orgId) {
        Query query=new Query(Criteria.where("status").nin(Status.DELETED.label).and("orgId").is(orgId));
        return mongoTemplate.find(query, PayrollFBPRange.class, COLLECTION_NAME);
    }

    default Optional<PayrollFBPRange> findBySalaryRange(int grossEarnings, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query().addCriteria(new Criteria().andOperator(
                Criteria.where("from").lte(grossEarnings),
                new Criteria().orOperator(
                        Criteria.where("to").gte(grossEarnings),
                        Criteria.where("to").is(0)
                ),
                Criteria.where("orgId").is(orgId)
        ));

        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPRange.class, COLLECTION_NAME));
    }

}
