package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.payroll.PayrollMonthlySuppVariablesDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.PayrollMonthlyAndSuppVariables;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.*;
import java.util.stream.Collectors;

public interface PayrollMonthlyVariableRepository extends MongoRepository<PayrollMonthlyAndSuppVariables, String> {
    static final String COLLECTION_NAME = "payroll_t_monthly_supplementary_variables_";
    default void bulkUpsert(List<PayrollMonthlyAndSuppVariables> dtoList, MongoTemplate mongoTemplate, String orgId) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PayrollMonthlyAndSuppVariables.class, COLLECTION_NAME + orgId);
        for (PayrollMonthlyAndSuppVariables dto : dtoList) {
            Query query = new Query(Criteria.where("empId").is(dto.getEmpId())
                    .and("payrollMonth").is(dto.getPayrollMonth())
                    .and("variableType").is(dto.getVariableType())
            );
            Update update = buildUpdateFromDTO(dto);
            bulkOperations.upsert(query, update);
        }
//        bulkOperations.insert(dtoList);
        bulkOperations.execute();

    }
    default  BulkWriteResult bulkSuppUpsert(MongoTemplate mongoTemplate, String orgId, List<Map<String, Object>> data, String type, String payrollMonth, List<String> components) {

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PayrollMonthlyAndSuppVariables.class, COLLECTION_NAME + orgId);

        for (Map<String, Object> value : data) {

            String empId = value.get("Employee_ID").toString();
            Map<String, Integer> column = new HashMap<>();

            components.forEach(comp -> {
                int val = (int) value.getOrDefault(comp, 0);
                column.put(comp.replaceAll("_\\(.*\\)", "").toLowerCase(), val);
            });

            Query query = new Query(Criteria.where("empId").is(empId)
                    .and("payrollMonth").is(payrollMonth)
                    .and("variableType").is(type)
            );

            Update update = new Update()
                    .set("empId", empId)
                    .set("payrollMonth", payrollMonth)
                    .set("variableType", type)
                    .set("componentValues", column);

            bulkOperations.upsert(query, update);
        }
//        bulkOperations.insert(dtoList);

        return   bulkOperations.execute();

    }
    default boolean upsert(PayrollMonthlyAndSuppVariables dto, MongoTemplate mongoTemplate, String orgId) {

        Query query = new Query(Criteria.where("empId").is(dto.getEmpId())
                .and("payrollMonth").is(dto.getPayrollMonth())
                .and("variableType").is(dto.getVariableType())
        );
        Update update = buildUpdateFromDTO(dto);
        UpdateResult result = mongoTemplate.upsert(query, update, PayrollMonthlyAndSuppVariables.class, COLLECTION_NAME + orgId);
        return result.wasAcknowledged();

    }
    default List<PayrollMonthlySuppVariablesDTO> getByPayrollMonth(MongoTemplate mongoTemplate,String orgCode, String payrollMonth, String variableType ){
        String collection = COLLECTION_NAME + orgCode;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId", "empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(Criteria.where("variableType").is(variableType)
                        .and("payrollMonth").is(payrollMonth)
                        .and("users.status").is(Status.ACTIVE.label)
                ),
                Aggregation.project()
                        .and("empId").as("empId")
                        .and("variableType").as("variableType")
                        .and("payrollMonth").as("payrollMonth")
                        .and("componentValues").as("componentValues")
                        .andExpression("concat(ifNull(users.sections.basicDetails.firstName, ''), ' ', ifNull(users.sections.basicDetails.middleName, ''), ' ', ifNull(users.sections.basicDetails.lastName, ''))").as("empName")
                        .andExclude("_id")
        );

        return mongoTemplate.aggregate(aggregation, collection, PayrollMonthlySuppVariablesDTO.class).getMappedResults();

    }
    static Update buildUpdateFromDTO(PayrollMonthlyAndSuppVariables dto) {
        return new Update()
                .set("empId", dto.getEmpId())
                .set("payrollMonth", dto.getPayrollMonth())
                .set("variableType", dto.getVariableType())
                .set("componentValues", dto.getComponentValues());

    }
}
