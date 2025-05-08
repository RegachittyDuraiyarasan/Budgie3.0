package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollFBPComponentRepository extends MongoRepository<PayrollFBPComponentMaster, String> {
    static final String COLLECTION_NAME = "payroll_m_fbp_component_master";

    default Optional<PayrollFBPComponentMaster> findLatestComponent(String orgId, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPComponentMaster.class, COLLECTION_NAME));
    }
    default Optional<PayrollFBPComponentMaster> existsByComponentNameActive(String type, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("componentName").is(type))
                .addCriteria(Criteria.where("orgId").is(orgId))
                .addCriteria(Criteria.where("status").is(Status.ACTIVE.label));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPComponentMaster.class, COLLECTION_NAME));
    }
    default boolean existsByComponentNameAndStatus(String type, MongoTemplate mongoTemplate, PayrollFBPComponentMaster dto) {
        Query query = new Query()
                .addCriteria(Criteria.where("componentName").is(dto.getComponentName()))
                .addCriteria(Criteria.where("orgId").is(dto.getOrgId()))
                .addCriteria(Criteria.where("status").nin(Status.DELETED.label));
        if(type.equalsIgnoreCase("update"))
            query.addCriteria(Criteria.where("componentId").nin(dto.getComponentId()));
        return mongoTemplate.exists(query, PayrollFBPComponentMaster.class, COLLECTION_NAME);
    }
    default boolean upsert(PayrollFBPComponentMaster request, MongoTemplate mongoTemplate){
        Query query = new Query(Criteria.where("componentId").is(request.getComponentId()));
        Update update = buildUpdateFromDTO(request);
        UpdateResult result = mongoTemplate.upsert(query, update, PayrollFBPComponentMaster.class, COLLECTION_NAME);
        return result.wasAcknowledged();
    }

    static Update buildUpdateFromDTO(PayrollFBPComponentMaster request) {
        return new Update()
                .set("componentName", request.getComponentName())
                .set("componentSlug", request.getComponentSlug())
                .set("status", request.getStatus())
                .set("orgId", request.getOrgId())
                .set("_class", PayrollFBPComponentMaster.class.getName());
    }

    default List<PayrollFBPComponentMaster> findByNonDeleteList(MongoTemplate mongoTemplate, String orgId) {
        Query query=new Query(Criteria.where("status").nin(Status.DELETED.label).and("orgId").is(orgId));
        return mongoTemplate.find(query, PayrollFBPComponentMaster.class, COLLECTION_NAME);
    }

    default Optional<PayrollFBPComponentMaster> findByComponentId(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("componentId").is(id))
                .addCriteria(Criteria.where("orgId").is(orgId));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPComponentMaster.class, COLLECTION_NAME));
    }

    default boolean updateStatus(String componentId, String newStatus, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentId").is(componentId));
        Update update = new Update().set("status", newStatus);

        UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollFBPComponentMaster.class, COLLECTION_NAME);
        return result.getModifiedCount() > 0;
    }
}
