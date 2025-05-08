package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.PayrollLwf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.entity.payroll.payrollEnum.DeductionType;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

public interface PayrollLwfRepository extends MongoRepository<PayrollLwf, String> {
    public static final String COLLECTION_NAME = "payroll_m_lwf_";

    default Optional<PayrollLwf> findTopByOrderByIdDesc(String country, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PayrollLwf result = mongoTemplate.findOne(query, PayrollLwf.class, COLLECTION_NAME + country);
        return Optional.ofNullable(result);
    }

    default Optional<PayrollLwf> findByLwfId(MongoTemplate mongoTemplate, String country, String lwfId, String orgId) {

        Query query = new Query(Criteria.where("lwfId").is(lwfId)
                .and("status").nin(Status.DELETED.label)
                .and("orgId").in(orgId)
        );
        PayrollLwf result = mongoTemplate.findOne(query, PayrollLwf.class, COLLECTION_NAME + country);

        return Optional.ofNullable(result);
    }

    default boolean upsert(PayrollLwfDTO dto, MongoTemplate mongoTemplate, String country) {

        Query query = new Query(Criteria.where("lwfId").is(dto.getLwfId()));

        Update update = new Update();
        update.set("lwfId", dto.getLwfId());
        update.set("state", dto.getState());
        update.set("deductionType", dto.getDeductionType());
        update.set("deductionMonth", dto.getDeductionMonth());
        update.set("employeeContribution", dto.getEmployeeContribution());
        update.set("employerContribution", dto.getEmployerContribution());
        update.set("totalContribution", dto.getTotalContribution());
        update.set("orgId", dto.getOrgId());
        update.set("status", dto.getStatus());
        update.set("_class", PayrollLwf.class.getName());

        UpdateResult updateResult = mongoTemplate.upsert(query, update, PayrollLwf.class, COLLECTION_NAME + country);

        return updateResult.wasAcknowledged();

    }

    default boolean existsByStateAndOrgIdIn(MongoTemplate mongoTemplate, PayrollLwfDTO dto, String country, String operation) {
        Query query = new Query();
        query.addCriteria(Criteria.where("state").is(dto.getState()));
        query.addCriteria(Criteria.where("status").nin(Status.DELETED.label));
        query.addCriteria(Criteria.where("orgId").is(dto.getOrgId()));

        if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {
            query.addCriteria(Criteria.where("lwfId").nin(dto.getLwfId()));
        }

        return mongoTemplate.exists(query, PayrollLwf.class, COLLECTION_NAME + country);
    }

    default void deleteLwf(String id, MongoTemplate mongoTemplate, String country) {
        Query query = new Query(Criteria.where("lwfId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        mongoTemplate.findAndModify(query, update, PayrollLwf.class, COLLECTION_NAME + country);
    }

    default List<PayrollLwf> findByOrgId(MongoTemplate mongoTemplate, String country, String orgCode) {

        Query query = new Query(Criteria.where("orgId").in(orgCode).
                and("status").nin(Status.DELETED.label));

        return mongoTemplate.find(query, PayrollLwf.class, COLLECTION_NAME + country);
    }

}
