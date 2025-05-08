package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollESICDto;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollESIC;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollESICRepository extends MongoRepository<PayrollESIC, String> {
    public static final String COLLECTION_NAME = "payroll_m_esic_";

    default Optional<PayrollESIC> findTopByOrderByIdDesc(String country, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PayrollESIC result = mongoTemplate.findOne(query, PayrollESIC.class, COLLECTION_NAME + country);
        return Optional.ofNullable(result);
    }

    default Optional<PayrollESIC> findByEsicId(MongoTemplate mongoTemplate, String country, String esicId, String orgId) {
        Query query = new Query(Criteria.where("esicId").is(esicId).and("orgId").in(orgId)
                .and("status").nin(Status.DELETED.label));
        PayrollESIC result = mongoTemplate.findOne(query, PayrollESIC.class, COLLECTION_NAME + country);
        return Optional.ofNullable(result);
    }

    default boolean upsert(PayrollESICDto dto, MongoTemplate mongoTemplate, String country) {
        Query query = new Query(Criteria.where("esicId").is(dto.getEsicId()));
        Update update = new Update();
        update.set("esicId", dto.getEsicId());
        update.set("employeeContribution", dto.getEmployeeContribution());
        update.set("employerContribution", dto.getEmployerContribution());
        update.set("orgId", dto.getOrgId());
        update.set("status", dto.getStatus());
        update.set("_class", PayrollESIC.class.getName());
        UpdateResult updateResult = mongoTemplate.upsert(query, update, PayrollESIC.class, COLLECTION_NAME + country);
        return updateResult.wasAcknowledged();

    }

    default boolean existsByOrgIdIn(MongoTemplate mongoTemplate, PayrollESICDto dto, String country, String operation) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").in(dto.getOrgId()));
        query.addCriteria(Criteria.where("status").nin(Status.DELETED.label, Status.INACTIVE.label));
        if (operation.equalsIgnoreCase("Update")) {
            query.addCriteria(Criteria.where("esicId").nin(dto.getEsicId()));
        }
        return mongoTemplate.exists(query, PayrollESIC.class, COLLECTION_NAME + country);
    }

    default boolean existsByOrgIdInAndStatus(MongoTemplate mongoTemplate, String country, String esicId,
            String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orgId").in(orgId));
        query.addCriteria(Criteria.where("status").nin(Status.DELETED.label, Status.INACTIVE.label));
        query.addCriteria(Criteria.where("esicId").nin(esicId));
        return mongoTemplate.exists(query, PayrollESIC.class, COLLECTION_NAME + country);
    }

    default List<PayrollESIC> findByOrgId(MongoTemplate mongoTemplate, String country, String organization) {
        Query query = new Query(Criteria.where("orgId").in(organization).and("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, PayrollESIC.class, COLLECTION_NAME + country);
    }

    default void updateStatus(String id, MongoTemplate mongoTemplate, String country, String status) {
        Query query = new Query(Criteria.where("esicId").is(id));
        Update update = new Update();
        update.set("status", status);
        mongoTemplate.findAndModify(query, update, PayrollESIC.class, COLLECTION_NAME + country);
    }

}
