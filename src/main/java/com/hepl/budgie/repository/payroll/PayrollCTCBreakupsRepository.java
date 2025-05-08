package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsListDTO;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.dto.payroll.PayrollPaysheetDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollCTCBreakups;
import com.mongodb.bulk.BulkWriteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface PayrollCTCBreakupsRepository extends MongoRepository<PayrollCTCBreakups, String> {
    public static final String COLLECTION_NAME = "payroll_t_ctc_breakups_";
    default BulkWriteResult bulkUpsert(List<PayrollCTCBreakupsDTO> dtoList, MongoTemplate mongoTemplate, String orgId) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PayrollCTCBreakups.class, COLLECTION_NAME + orgId);
        for (PayrollCTCBreakupsDTO dto : dtoList) {
            Query query = new Query(Criteria.where("empId").is(dto.getEmpId())
                    .and("revisionOrder").is(dto.getRevisionOrder())
                    .and("withEffectDate").is(dto.getWithEffectDate().toInstant())
            );
            Update update = buildUpdateFromDTO(dto);
            bulkOperations.upsert(query, update);
        }
//        bulkOperations.insert(dtoList);
        return bulkOperations.execute();

    }

    static Update buildUpdateFromDTO(PayrollCTCBreakupsDTO dto) {
        return new Update()
                .set("empId", dto.getEmpId())
                .set("withEffectDate", dto.getWithEffectDate())
                .set("financialYear", dto.getFinancialYear())
                .set("payrollMonth", dto.getPayrollMonth())
                .set("earningColumns", dto.getEarningColumns())
                .set("deductionColumn", dto.getDeductionColumn())
                .set("grossEarnings", dto.getGrossEarnings())
                .set("grossDeductions", dto.getGrossDeductions())
                .set("netPay", dto.getNetPay())
                .set("revisionOrder", dto.getRevisionOrder())
                .set("delete", false);
//                .set("_class", PayrollCTCBreakups.class);
    }

    default Optional<PayrollCTCBreakups> findLatestEmpCtc(MongoTemplate mongoTemplate, String orgCode, String empId){
        Query query = new Query(Criteria.where("empId").is(empId));
        query.with(Sort.by(Sort.Direction.DESC, "empId"));
        query.limit(1);
        PayrollCTCBreakups result = mongoTemplate.findOne(query, PayrollCTCBreakups.class, COLLECTION_NAME + orgCode);
        return Optional.ofNullable(result);
    }
    default List<PayrollCTCBreakupsDTO> findAllByOrgIdWithUserInfo(MongoTemplate mongoTemplate , String orgId){
        String ctcBreakupCollection = COLLECTION_NAME + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId","empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(Criteria.where("users.status").is(Status.ACTIVE.label).and("users.organization.organizationCode").is(orgId)),
                Aggregation.sort(Sort.Direction.DESC, "revisionOrder"),
                Aggregation.group("empId")
                        .first(Aggregation.ROOT).as("ctc")
                        .first("users.sections.basicDetails.firstName").as("firstName")
                        .first("users.sections.basicDetails.middleName").as("middleName")
                        .first("users.sections.basicDetails.lastName").as("lastName"),

                Aggregation.project()
                        .and("ctc.empId").as("empId")
                        .and("ctc.financialYear").as("financialYear")
                        .and("ctc.withEffectDate").as("withEffectDate")
                        .and("ctc.earningColumns").as("earningColumns")
                        .and("ctc.deductionColumn").as("deductionColumn")
                        .and("ctc.grossEarnings").as("grossEarnings")
                        .and("ctc.grossDeductions").as("grossDeductions")
                        .and("ctc.netPay").as("netPay")
                        .and("ctc.revisionOrder").as("revisionOrder")
                        .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(middleName, ''), ' ', ifNull(lastName, ''))").as("empName")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation, ctcBreakupCollection, PayrollCTCBreakupsDTO.class)
                .getMappedResults();

    }

    default List<PayrollCTCBreakupsDTO> findByEmpId(MongoTemplate mongoTemplate , String orgId, String empId){
        String ctcBreakupCollection = COLLECTION_NAME + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId","empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(
                        Criteria.where("users.status").is(Status.ACTIVE.label)
                                .and("users.organization.organizationCode").is(orgId)
                                .and("empId").is(empId)
                ),
                Aggregation.sort(Sort.Direction.DESC, "revisionOrder"),


                Aggregation.project()
                        .and("empId").as("empId")
                        .and("financialYear").as("financialYear")
                        .and("withEffectDate").as("withEffectDate")
                        .and("earningColumns").as("earningColumns")
                        .and("deductionColumn").as("deductionColumn")
                        .and("grossEarnings").as("grossEarnings")
                        .and("grossDeductions").as("grossDeductions")
                        .and("netPay").as("netPay")
                        .and("revisionOrder").as("revisionOrder")
                        .andExpression("concat(ifNull(users.sections.basicDetails.firstName, ''), ' ', ifNull(users.sections.basicDetails.middleName, ''), ' ', ifNull(users.sections.basicDetails.lastName, ''))").as("empName")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation, ctcBreakupCollection, PayrollCTCBreakupsDTO.class)
                .getMappedResults();

    }

    default List<PayrollCTCBreakupsDTO> getNewJoinerCTC(MongoTemplate mongoTemplate , String orgId, ZonedDateTime startDate, ZonedDateTime endDate){
        String ctcBreakupCollection = COLLECTION_NAME + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup(ctcBreakupCollection, "empId","empId", "ctcBreakups"),
                Aggregation.unwind("ctcBreakups", true),
                Aggregation.match(Criteria.where("status").is(Status.ACTIVE.label)
                        .and("organization.organizationCode").is(orgId)
                        .and("sections.workingInformation.doj").gte(startDate.toInstant()).lte(endDate.toInstant())
                ),
                Aggregation.sort(Sort.Direction.DESC, "ctcBreakups.revisionOrder"),
                Aggregation.group("empId")
                        .first("ctcBreakups").as("ctc")
                        .first(Aggregation.ROOT).as("users"),

                Aggregation.project()
                        .and("users.empId").as("empId")
                        .and("users.sections.workingInformation.doj").as("doj")
                        .and("ctc.financialYear").as("financialYear")
                        .and("ctc.withEffectDate").as("withEffectDate")
                        .and("ctc.earningColumns").as("earningColumns")
                        .and("ctc.deductionColumn").as("deductionColumn")
                        .and("ctc.grossEarnings").as("grossEarnings")
                        .and("ctc.grossDeductions").as("grossDeductions")
                        .and("ctc.netPay").as("netPay")
                        .and("ctc.revisionOrder").as("revisionOrder")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation, "userinfo", PayrollCTCBreakupsDTO.class)
                .getMappedResults();

    }

    default List<PayrollCTCBreakupsListDTO> findByEmpIdIn(MongoTemplate mongoTemplate , String orgId, List<String> empId ){
        String ctcBreakupCollection = COLLECTION_NAME + orgId;

        Aggregation aggregation = Aggregation.newAggregation(

                Aggregation.lookup(ctcBreakupCollection, "empId","empId", "ctcBreakups"),
//                Aggregation.unwind( "ctcBreakups", true),

                Aggregation.match(
                        Criteria.where("status").is(Status.ACTIVE.label)
                                .and("organization.organizationCode").is(orgId)
                                .and("empId").in(empId)
                ),

                Aggregation.project()
                        .and("empId").as("empId")
                        .and("sections.workingInformation.doj").as("doj")
                        .and("ctcBreakups").as("ctcBreakups")

//                        .and(ConditionalOperators.ifNull("ctcBreakups").then(new ArrayList<>())).as("ctcBreakups")
                        .andExpression("concat(ifNull(sections.basicDetails.firstName, ''), ' ', ifNull(sections.basicDetails.middleName, ''), ' ', ifNull(sections.basicDetails.lastName, ''))").as("empName")
                        .andExclude("_id")

        );

        return mongoTemplate.aggregate(aggregation, "userinfo", PayrollCTCBreakupsListDTO.class)
                .getMappedResults();

    }


    default Optional<PayrollCTCBreakups> findByEmpIdAndRevisionOrderDesc(String empId, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query()
                .addCriteria(Criteria.where("empId").is(empId))
                .with(Sort.by(Sort.Order.desc("revisionOrder")));

        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollCTCBreakups.class, COLLECTION_NAME + orgId));
    }

    default List<EmployeeActiveDTO> employeeList(MongoTemplate mongoTemplate, String orgId) {
        String ctcBreakupCollection = COLLECTION_NAME + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId","empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(
                        Criteria.where("users.status").is(Status.ACTIVE.label)
                                .and("users.empIdGenerateStatus").is(true)
                                .and("users.organization.organizationCode").is(orgId)
                                .and("grossEarnings").gte(700000)
                ),
                Aggregation.sort(Sort.Direction.DESC, "revisionOrder"),
                Aggregation.group("empId")
                        .first(Aggregation.ROOT).as("ctc")
                        .first("users.sections.basicDetails.firstName").as("firstName")
                        .first("users.sections.basicDetails.middleName").as("middleName")
                        .first("users.sections.basicDetails.lastName").as("lastName"),

                Aggregation.project()
                        .and("ctc.empId").as("empId")
                        .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(middleName, ''), ' ', ifNull(lastName, ''))").as("employeeName")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation, ctcBreakupCollection, EmployeeActiveDTO.class)
                .getMappedResults();
    }

    default List<EmployeeActiveDTO> activeEmployeeList(MongoTemplate mongoTemplate, String orgId) {
        String fbpPlanCollection  = "payroll_t_fbp_plan_" + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId","empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(
                        Criteria.where("users.status").is(Status.ACTIVE.label)
                                .and("users.empIdGenerateStatus").is(true)
                                .and("users.organization.organizationCode").is(orgId)
                ),
                Aggregation.lookup(fbpPlanCollection,"empId", "empId" ,"fpbPlans"),
                Aggregation.match(
                        Criteria.where("fpbPlans.status").nin("Deleted","Created")
                ),
                Aggregation.sort(Sort.Direction.DESC, "revisionOrder"),
                Aggregation.group("empId")
                        .first(Aggregation.ROOT).as("ctc")
                        .first("users.sections.basicDetails.firstName").as("firstName")
                        .first("users.sections.basicDetails.middleName").as("middleName")
                        .first("users.sections.basicDetails.lastName").as("lastName"),

                Aggregation.project()
                        .and("ctc.empId").as("empId")
                        .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(middleName, ''), ' ', ifNull(lastName, ''))").as("employeeName")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation, fbpPlanCollection, EmployeeActiveDTO.class)
                .getMappedResults();
    }

    default List<EmployeeActiveDTO> considerEmployeeList(MongoTemplate mongoTemplate,String orgId) {
        String fbpPlanCollection  = "payroll_t_fbp_plan_" + orgId;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.lookup("userinfo", "empId","empId", "users"),
                Aggregation.unwind("users"),
                Aggregation.match(
                        Criteria.where("users.status").is(Status.ACTIVE.label)
                                .and("users.empIdGenerateStatus").is(true)
                                .and("users.organization.organizationCode").is(orgId)
                ),
                Aggregation.lookup(fbpPlanCollection,"empId", "empId" ,"fpbPlans"),
                Aggregation.match(
                        Criteria.where("fpbPlans.status").is("Consider")
                ),
                Aggregation.sort(Sort.Direction.DESC, "revisionOrder"),
                Aggregation.group("empId")
                        .first(Aggregation.ROOT).as("ctc")
                        .first("users.sections.basicDetails.firstName").as("firstName")
                        .first("users.sections.basicDetails.middleName").as("middleName")
                        .first("users.sections.basicDetails.lastName").as("lastName"),

                Aggregation.project()
                        .and("ctc.empId").as("empId")
                        .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(middleName, ''), ' ', ifNull(lastName, ''))").as("employeeName")
                        .andExclude("_id")

        );
        return mongoTemplate.aggregate(aggregation,fbpPlanCollection , EmployeeActiveDTO.class)
                .getMappedResults();
    }


    default List<PayrollPaysheetDTO> getPaysheetDetails(String type, PayrollMonth payrollMonth, MongoTemplate mongoTemplate, String orgId) {

        String attendanceMusterInfo = "attendance_muster_" + orgId;
        String monthlyImport = "payroll_t_monthly_supplementary_variables_" + orgId;
        String arrears = "payroll_t_arrears_" + orgId;
        String vpf = "payroll_t_vpf_nps_" + orgId;
        String loan = "payroll_t_emp_loans_" + orgId;
        List<AggregationOperation> operations = new ArrayList<>();

        // Add lookup and unwind operations
        operations.add(Aggregation.lookup("userinfo", "empId", "empId", "users"));
        operations.add(Aggregation.lookup(attendanceMusterInfo, "empId", "empId", "attendanceMuster"));
        operations.add(Aggregation.lookup(monthlyImport, "empId", "empId", "payrollMonthlyImports"));
        operations.add(Aggregation.lookup(arrears, "empId", "empId", "payrollArrears"));
        operations.add(Aggregation.lookup(vpf, "empId", "empId", "payrollVpf"));
        operations.add(Aggregation.lookup(loan, "empId", "empId", "payrollLoan"));
        operations.add(Aggregation.unwind("users", true));
        operations.add(Aggregation.unwind("attendanceMuster", true));
        operations.add(Aggregation.unwind("payrollMonthlyImports", true));
        operations.add(Aggregation.unwind("payrollArrears", true));
        operations.add(Aggregation.unwind("payrollVpf", true));
        operations.add(Aggregation.unwind("payrollLoan", true));

        // Add match operations
        operations.add(Aggregation.match(Criteria.where("users.status").is(Status.ACTIVE.label)));

        if (!"All".equals(type)) {
            operations.add(Aggregation.match(Criteria.where("users.sections.workingInformation.payrollStatusName").is(type)));
        }

//        operations.add(Aggregation.match(Criteria.where("attendanceMuster.finYear").is(payrollMonth.getFinYear())));
        operations.add(Aggregation.match(Criteria.where("users.sections.workingInformation.doj").lte(payrollMonth.getEndDate().toInstant())));

        // Add project and group operations
        operations.add(Aggregation.project()
                .and("users").as("users")
                .and("attendanceMuster").as("attendanceMuster")
                .and("payrollMonthlyImports").as("payrollMonthlyImports")
                .and("payrollArrears").as("payrollArrears")
                .and("payrollVpf").as("payrollVpf")
                .and("payrollLoan").as("payrollLoan")
                .and(Aggregation.ROOT).as("payrollCtcBreakUps"));
        operations.add(Aggregation.group("payrollCtcBreakUps.empId")
                .last("payrollCtcBreakUps").as("payrollCtcBreakUps")
                .last("users").as("users")
                .addToSet("attendanceMuster").as("attendanceMuster")
                .last("payrollArrears").as("payrollArrears")
                .last("payrollVpf").as("payrollVpf")
                .last("payrollLoan").as("payrollLoan")
                .addToSet("payrollMonthlyImports").as("payrollMonthlyImports"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, COLLECTION_NAME + orgId, PayrollPaysheetDTO.class).getMappedResults();
    }

    default void initCTCIndexing(String org, MongoTemplate mongoTemplate) {
        String ctcBreakupCollection = COLLECTION_NAME + org;
        mongoTemplate.indexOps(ctcBreakupCollection).ensureIndex(
                new Index()
                        .on("empId", Sort.Direction.ASC)
                        .on("revisionOrder", Sort.Direction.ASC)
                        .unique()
        );

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(ctcBreakupCollection));

        mongoTemplate.aggregate(aggregation, COLLECTION_NAME, PayrollCTCBreakups.class);
    }
}
