package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.dto.payroll.PayrollMonthlySuppVariablesDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollArrears;
import com.hepl.budgie.entity.payroll.PayrollMonthlyAndSuppVariables;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PayrollArrearsRepository extends MongoRepository<PayrollArrears, String> {

    static final String COLLECTION_NAME = "payroll_t_arrears_";
    default BulkWriteResult bulkUpsert(MongoTemplate mongoTemplate, String orgId, List<PayrollArrears> dtoList) {

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                PayrollArrears.class,
                COLLECTION_NAME + orgId);

        // Delete existing records for Emp ID and Payroll Month
        Query deleteQuery = new Query(Criteria.where("empId").in(dtoList.stream().map(PayrollArrears::getEmpId).toList())
                .and("payrollMonth").in(dtoList.stream().map(PayrollArrears::getPayrollMonth).toList()));
        mongoTemplate.remove(deleteQuery, PayrollArrears.class, COLLECTION_NAME + orgId);

        // Insert new records
        bulkOperations.insert(dtoList);

        return bulkOperations.execute();

    }
    static Update buildUpdateFromDTO(PayrollArrears dto) {
        return new Update()
                .set("empId", dto.getEmpId())
                .set("payrollMonth", dto.getPayrollMonth())
                .set("withEffectDate", dto.getWithEffectDate())
                .set("gross", dto.getGross())
                .set("arrearsValues", dto.getArrearsValues())
                .set("arrType", dto.getArrType())
                .set("arrDays", dto.getArrDays());

    }

    default List<PayrollArrears> getByPayrollMonth(MongoTemplate mongoTemplate, String orgCode, String payrollMonth ){
        String collection = COLLECTION_NAME + orgCode;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId", "empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(
                        Criteria.where("payrollMonth").is(payrollMonth)
                                .and("users.status").is(Status.ACTIVE.label)
                ),
                Aggregation.project()
                        .and("empId").as("empId")
                        .and("payrollMonth").as("payrollMonth")
                        .and("withEffectDate").as("withEffectDate")
                        .and("arrearsValues").as("arrearsValues")
                        .and("gross").as("gross")
                        .and("arrDays").as("arrDays")
                        .and("arrType").as("arrType")
                        .andExpression("concat(ifNull(users.sections.basicDetails.firstName, ''), ' ', ifNull(users.sections.basicDetails.middleName, ''), ' ', " +
                                "ifNull(users.sections.basicDetails.lastName, ''))").as("empName")
                        .andExclude("_id")
        );

        return mongoTemplate.aggregate(aggregation, collection, PayrollArrears.class).getMappedResults();

    }
}
