package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.dto.payroll.PayrollVpfDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollESIC;
import com.hepl.budgie.entity.payroll.PayrollLwf;
import com.hepl.budgie.entity.payroll.PayrollVpf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollVpfRepository  extends MongoRepository<PayrollVpf, String> {
    public static final String COLLECTION_NAME = "payroll_t_vpf_nps_";
    default Optional<PayrollVpf> findTopByOrderByIdDesc(String orgId, MongoTemplate mongoTemplate){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PayrollVpf result = mongoTemplate.findOne(query, PayrollVpf.class, COLLECTION_NAME+orgId);
        return Optional.ofNullable(result);
    }
    default  Optional<PayrollVpf> findByRcpfId(MongoTemplate mongoTemplate,String orgId,String rcpfId){
        Query query = new Query(Criteria.where("rcpfId").is(rcpfId).and("status").nin(Status.DELETED.label));
        PayrollVpf result = mongoTemplate.findOne(query, PayrollVpf.class, COLLECTION_NAME +orgId);
        return Optional.ofNullable(result);
    }
    default boolean upsert(PayrollVpfDTO dto, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("rcpfId").is(dto.getRcpfId()));
        Update update = new Update();
        update.set("rcpfId",dto.getRcpfId());
        update.set("empId",dto.getEmpId());
        update.set("type",dto.getType());
        update.set("deductionType",dto.getDeductionType());
        if(dto.getDeductionType().equalsIgnoreCase("Amount")){
            update.set("amount",dto.getAmount());
        }
        if(dto.getDeductionType().equalsIgnoreCase("Percentage")){
            update.set("percentage",dto.getPercentage());
        }
        update.set("fromMonth",dto.getFromMonth());
        update.set("toMonth",dto.getToMonth());
//        update.set("hrEmpId",dto.getOrgId());
        update.set("status",dto.getStatus());
//        update.set("authorizedOn",dto.getStatus());
        update.set("_class",PayrollVpf.class.getName());
        UpdateResult updateResult= mongoTemplate.upsert(query, update, PayrollVpf.class,COLLECTION_NAME + orgId);
        return updateResult.wasAcknowledged();

    }
    default boolean existsByEmpIdAndType(MongoTemplate mongoTemplate, PayrollVpfDTO dto, String orgId, String operation) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(dto.getEmpId()));
        query.addCriteria(Criteria.where("type").is(dto.getType()));
        if(operation.equalsIgnoreCase(DataOperations.UPDATE.label)){
            query.addCriteria(Criteria.where("rcpfId").nin(dto.getRcpfId()));
        }
        return mongoTemplate.exists(query,PayrollVpf.class, COLLECTION_NAME+orgId);
    }


    default List<PayrollVpf> findByTypeAndStatus(MongoTemplate mongoTemplate, String orgId, String type) {
        Query query = new Query(
                Criteria.where("type").is(type).
                        and("status").nin(Status.DELETED.label)
        );
        return mongoTemplate.find(query, PayrollVpf.class, COLLECTION_NAME + orgId);
    }
    default void updateStatus(String id, MongoTemplate mongoTemplate, String orgId,String status) {
        Query query = new Query(Criteria.where("rcpfId").is(id));
        Update update = new Update();
        update.set("status", status);
        mongoTemplate.findAndModify(query, update, PayrollVpf.class, COLLECTION_NAME + orgId);
    }

}
