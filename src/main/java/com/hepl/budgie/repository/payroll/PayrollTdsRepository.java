package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollTdsDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollTds;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.entity.payroll.payrollEnum.TaxType;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollTdsRepository extends MongoRepository<PayrollTds, String> {
    public static final String COLLECTION_NAME = "payroll_m_tds_slab_";

    default Optional<PayrollTds> findTopByOrderByIdDesc(String country, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PayrollTds result = mongoTemplate.findOne(query, PayrollTds.class, COLLECTION_NAME + country);
        return Optional.ofNullable(result);
    }

    default Optional<PayrollTds> findByTdsSlabId(MongoTemplate mongoTemplate, String country, String lwfId, String orgCode) {
        Query query = new Query(
                Criteria.where("tdsSlabId").is(lwfId)
                        .and("status").nin(Status.DELETED.label)
                        .and("orgId").is(orgCode)
        );
        PayrollTds result = mongoTemplate.findOne(query, PayrollTds.class, COLLECTION_NAME + country);
        return Optional.ofNullable(result);
    }

    default boolean upsert(PayrollTdsDTO dto, MongoTemplate mongoTemplate, String country) {

        Query query = new Query(Criteria.where("tdsSlabId").is(dto.getTdsSlabId()));
        Update update = new Update();
        update.set("tdsSlabId", dto.getTdsSlabId());
        update.set("type", dto.getType());
        update.set("regime", dto.getRegime());
        update.set("percentage", dto.getPercentage());

        if (dto.getType().equalsIgnoreCase(TaxType.TAX.label)) {
            update.set("ageLimit", dto.getAgeLimit());
            update.set("taxAmount", dto.getTaxAmount());
        }

        if (dto.getType().equalsIgnoreCase(TaxType.TAX.label) || dto.getType().equalsIgnoreCase(TaxType.SURCHARGE.label)) {
            update.set("salaryFrom", dto.getSalaryFrom());
            update.set("salaryTo", dto.getSalaryTo());
        }

        update.set("orgId", dto.getOrgId());
        update.set("status", dto.getStatus());
        update.set("_class", PayrollTds.class.getName());

        UpdateResult updateResult = mongoTemplate.upsert(query, update, PayrollTds.class, COLLECTION_NAME + country);
        return updateResult.wasAcknowledged();

    }

    default boolean existsByTypeAndOrgIdIn(MongoTemplate mongoTemplate, PayrollTdsDTO dto, String country, String operation) {
        Query query = new Query(
                Criteria.where("type").is(dto.getType())
                        .and("orgId").is(dto.getOrgId())
                        .and("status").nin(Status.DELETED.label)
                        .and("regime").is(dto.getRegime())
        );
        if (dto.getType().equalsIgnoreCase(TaxType.TAX.label)) {
            query.addCriteria(Criteria.where("ageLimit").is(dto.getAgeLimit()));
        }
        if (dto.getType().equalsIgnoreCase(TaxType.TAX.label) || dto.getType().equalsIgnoreCase(TaxType.SURCHARGE.label)) {
            query.addCriteria(Criteria.where("percentage").is(dto.getPercentage()));
            query.addCriteria(Criteria.where("salaryFrom").is(dto.getSalaryFrom()));
            query.addCriteria(Criteria.where("salaryTo").is(dto.getSalaryTo()));
        }
        if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {
            query.addCriteria(Criteria.where("tdsSlabId").nin(dto.getTdsSlabId()));
        }
        return mongoTemplate.exists(query, PayrollTds.class, COLLECTION_NAME + country);
    }

    default void deleteTdsSlab(String id, MongoTemplate mongoTemplate, String country) {
        Query query = new Query(Criteria.where("tdsSlabId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        mongoTemplate.findAndModify(query, update, PayrollTds.class, COLLECTION_NAME + country);
    }

    default void updateStatus(String id, MongoTemplate mongoTemplate, String country, String status) {
        Query query = new Query(Criteria.where("tdsSlabId").is(id));
        Update update = new Update();
        update.set("status", status);
        mongoTemplate.findAndModify(query, update, PayrollTds.class, COLLECTION_NAME + country);
    }

    default List<PayrollTds> findByOrgId(MongoTemplate mongoTemplate, String country, String organization) {
        Query query = new Query(Criteria.where("orgId").is(organization).
                and("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, PayrollTds.class, COLLECTION_NAME + country);
    }

}
