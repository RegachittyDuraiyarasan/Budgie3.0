package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPMaster;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollFBPMasterRepository extends MongoRepository<PayrollFBPMaster, String> {
    static final String COLLECTION_NAME = "payroll_m_fbp_master";
    default Optional<PayrollFBPMaster> findLatestComponent(MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPMaster.class, COLLECTION_NAME));
    }
    default List<PayrollFBPMaster> existsByFBPMaster(MongoTemplate mongoTemplate, String rangeId, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("rangeId").is(rangeId)
                .and("orgId").is(orgId)
                .and("status").is(Status.ACTIVE.label));
        return mongoTemplate.find(query, PayrollFBPMaster.class, COLLECTION_NAME);
    }

    default Optional<PayrollFBPMaster> findByRangeIdandFbpMaster(MongoTemplate mongoTemplate, PayrollFBPMaster request) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("rangeId").is(request.getRangeId())
                .and("fbpId").is(request.getFbpId())
                .and("orgId").is(request.getOrgId())
                .and("status").is(Status.ACTIVE.label));
        return Optional.ofNullable(
                mongoTemplate.findOne(query, PayrollFBPMaster.class, COLLECTION_NAME)
        );
    }

    default void upsert(PayrollFBPMaster request, MongoTemplate mongoTemplate) {
        mongoTemplate.save(request, COLLECTION_NAME);
    }

    default Optional<List<PayrollFBPMaster>> findByNonDeleteList(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("rangeId").is(id).and("status").is(Status.ACTIVE.label).and("orgId").is(orgId));
        List<PayrollFBPMaster> result = mongoTemplate.find(query, PayrollFBPMaster.class, COLLECTION_NAME);
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    default boolean update(MongoTemplate mongoTemplate, PayrollFBPMaster updatedMaster) {
        Query query = new Query(Criteria.where("fbpId").is(updatedMaster.getFbpId())); // Find by unique FBP ID
        Update update = new Update();

        // Only update non-null and non-empty fields
        if (updatedMaster.getFbpType() != null && !updatedMaster.getFbpType().isEmpty()) {
            update.set("fbpType", updatedMaster.getFbpType());
        }
        if (updatedMaster.getPayType() != null && !updatedMaster.getPayType().isEmpty()) {
            update.set("payType", updatedMaster.getPayType());
        }
        if (updatedMaster.getAmount() != 0) {
            update.set("amount", updatedMaster.getAmount());
        }

        // Ensure only modified fields are updated
        UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollFBPMaster.class, COLLECTION_NAME);
        return result.getModifiedCount() > 0;
    }

    default Optional<PayrollFBPMaster> findByComponentId(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("fbpId").is(id))
                .addCriteria(Criteria.where("orgId").is(orgId));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPMaster.class, COLLECTION_NAME));
    }

    default boolean updateStatus(String componentId, String newStatus, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("fbpId").is(componentId));
        Update update = new Update().set("status", newStatus);

        UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollFBPMaster.class, COLLECTION_NAME);
        return result.getModifiedCount() > 0;
    }
}
