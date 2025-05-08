package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.FbpCreatePlanDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollFBPPlan;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollFBPCreatePlanRepository extends MongoRepository<PayrollFBPPlan, String> {

    final String COLLECTION_NAME = "payroll_t_fbp_plan_";
    default Optional<PayrollFBPPlan> findLatestComponent(String orgId, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPPlan.class, COLLECTION_NAME + orgId));
    }
   default Optional<PayrollFBPPlan> findByEmpIdAndFinYear(String empId,String finYear, MongoTemplate mongoTemplate, String orgId) {
       Query query = new Query();
       query.addCriteria(Criteria.where("empId").is(empId).and("financialYear").is(finYear));

       return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPPlan.class,COLLECTION_NAME + orgId));
   }

    default void updateFBPPlan(String empId, String finYear, PayrollFBPPlan payrollFBPPlan, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("financialYear").is(finYear));

        Update update = new Update().set("endDate", payrollFBPPlan.getEndDate()).set("status", payrollFBPPlan.getStatus());
        mongoTemplate.findAndModify(query, update, PayrollFBPPlan.class, COLLECTION_NAME + orgId);
    }

    default void savePlan(PayrollFBPPlan payrollFBPPlan, MongoTemplate mongoTemplate, String orgId) {
       mongoTemplate.save(payrollFBPPlan, COLLECTION_NAME + orgId);
    }

    default boolean updateEmpPlan(String empId, String finYear, PayrollFBPPlan empPlan, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId).and("financialYear").is(finYear));

        Update update = new Update()
                .set("status", empPlan.getStatus())
                .set("fbp", empPlan.getFbp());

        PayrollFBPPlan updatedPlan = mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), PayrollFBPPlan.class, COLLECTION_NAME + orgId);

        return updatedPlan != null;
    }


    
    default List<FbpCreatePlanDTO> findByStatus(String status, MongoTemplate mongoTemplate,String finYear, String orgId) {

        String collection = COLLECTION_NAME + orgId;
        Criteria criteria=Criteria.where("userDetails.status").is(Status.ACTIVE.label)
                .and("userDetails.organization.organizationCode").is(orgId)
                .and("userDetails.empIdGenerateStatus").is(true)
                .and("payrollFBPPlans.financialYear").is(finYear)
                .and("payrollFBPPlans.status").is(status);


        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup(collection, "empId", "empId", "payrollFBPPlans"),
                Aggregation.unwind("payrollFBPPlans"),
                Aggregation.lookup("userinfo", "payrollFBPPlans.empId", "empId", "userDetails"),
                Aggregation.unwind("userDetails"),
                Aggregation.match(criteria),
                Aggregation.project()
                        .andExpression("payrollFBPPlans").as("payrollFBPPlans")
                        .andExpression("empId").as("empId")
                        .andExpression("fbpPlanId").as("fbpPlanId")
                        .andExpression("endDate").as("endDate")
                        .andExpression("concat(ifNull(userDetails.sections.basicDetails.firstName, ''), ' ', ifNull(userDetails.sections.basicDetails.middleName, ''), ' ', ifNull(userDetails.sections.basicDetails.lastName, ''))")
                        .as("empName"),
                Aggregation.unwind("payrollFBPPlans")
        );
        return mongoTemplate.aggregate(aggregation, collection, FbpCreatePlanDTO.class)
                .getMappedResults();
    }

    default Optional<PayrollFBPPlan> findByPlanIdAndEmployeeAndFinancialYear(String finYear, FbpCreatePlanDTO request, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("fbpPlanId").is(request.getFbpPlanId())
                .and("status").is(DataOperations.SUBMIT.label)
                .and("empId").is(request.getEmpId())
                .and("financialYear").is(finYear));

        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollFBPPlan.class, COLLECTION_NAME + orgId));
    }

    default void updateEmployeeConsiderPlan(String status, FbpCreatePlanDTO request, String finYear, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("fbpPlanId").is(request.getFbpPlanId())
                .and("empId").is(request.getEmpId())
                .and("financialYear").is(finYear));

        Update update = new Update().set("status", status);
        mongoTemplate.findAndModify(query, update, PayrollFBPPlan.class, COLLECTION_NAME + orgId);
    }


}
