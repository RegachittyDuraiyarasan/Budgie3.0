package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.dto.payroll.PayrollPaysheetDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.PayrollPaySheet;
import com.hepl.budgie.entity.payroll.payrollEnum.PayType;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.*;

public interface PayrollPaySheetRepository extends MongoRepository<PayrollPaySheet, String> {
    final String COLLECTION_NAME = "payroll_t_paysheet_";
    default List<PayrollPaySheet> findByEmpIdAndPayrollMonthIn(MongoTemplate mongoTemplate, String orgId, List<String> payrollMonth, String empId){
        Query query = new Query(
                Criteria.where("empId").in(empId)
                        .and("payrollMonth").in(payrollMonth))
                .with(Sort.by(Sort.Direction.ASC, "payrollMonth"));
        return mongoTemplate.find(query, PayrollPaySheet.class, COLLECTION_NAME + orgId);
    }

    default boolean existsByStatus(MongoTemplate mongoTemplate, String orgId, String status) {
        Query query = new Query(
                Criteria.where("status").is(status)
        );
        return mongoTemplate.exists(query, PayrollPaySheet.class, COLLECTION_NAME + orgId);
    }
    default List<PayrollPaysheetDTO> getPaySheetDetails(MongoTemplate mongoTemplate, String orgId, String type, PayrollMonth payrollMonth, List<String> empId) {

        String attendanceMusterInfo = "attendance_muster_" + orgId;
        String monthlyImport = "payroll_t_monthly_supplementary_variables_" + orgId;
        String arrears = "payroll_t_arrears_" + orgId;
        String vpf = "payroll_t_vpf_nps_" + orgId;
        String loan = "payroll_t_emp_loans_" + orgId;
        String ctcBreakups = "payroll_t_ctc_breakups_" + orgId;
        String pf = "payroll_m_pf_IN";

        // Filter Conditions
        Map<String, String> filterField = new HashMap<>();
        filterField.put("payrollMonth", payrollMonth.getPayrollMonth());
        Map<String, String> filterFieldMonthly = new HashMap<>(filterField);
        filterFieldMonthly.put("type", "Monthly");
        Map<String, String> filterFieldSupp = new HashMap<>(filterField);
        filterFieldSupp.put("type", "Monthly");


        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        new Criteria().andOperator(
                                Criteria.where("sections.workingInformation.doj").lte(payrollMonth.getEndDate().toInstant()),
                                new Criteria().orOperator(
                                        Criteria.where("status").is(Status.ACTIVE.label),
                                        Criteria.where("sections.workingInformation.dol").gte(payrollMonth.getStartDate().toInstant())
                                                .lte(payrollMonth.getEndDate().toInstant())
                                )
                        )
                ),

                buildLookupWithSortLimit(
                        ctcBreakups,
                        "ctcBreakUps",
                        2,
                        "revisionOrder"
                ),

                buildSimpleLookup(
                        monthlyImport,
                        "monthly",
                        "empId",
                        "empId",
                        filterFieldMonthly

                ),
                Aggregation.unwind("monthly", true),
                buildSimpleLookup(
                        monthlyImport,
                        "supplementary",
                        "empId",
                        "empId",
                        filterFieldSupp

                ),
                Aggregation.unwind("supplementary", true),

                buildSimpleLookup(
                        attendanceMusterInfo,
                        "attendanceMuster",
                        "empId",
                        "empId",
                        Collections.singletonMap("monthYear", payrollMonth.getPayrollMonth())
                ),
                Aggregation.unwind("attendanceMuster", true),

                buildSimpleLookup(
                        arrears,
                        "arrears",
                        "empId",
                        "empId",
                        Collections.singletonMap("payrollMonth", payrollMonth.getPayrollMonth())
                ),
                Aggregation.unwind("arrears", true),
                buildSimpleLookup(
                        pf,
                        "pf",
                        "pfId",
                        "payrollDetails.pfCode",
                        new HashMap<>()
                ),
                Aggregation.unwind("pf", true),

                buildSimpleLookup(
                        vpf,
                        "vpf",
                        "empId",
                        "empId",
                        Collections.singletonMap("type", "VPF")
                ),
                Aggregation.unwind("vpf", true),

                Aggregation.project()
                        .and("ctcBreakUps").as("ctcBreakUps")
                        .and("attendanceMuster").as("attendanceMuster")
                        .and("supplementary").as("supplementary")
                        .and("monthly").as("monthly")
                        .and("arrears").as("arrears")
                        .and("vpf").as("payrollVpf")
//                        .and("payrollLoan").as("payrollLoan")
                        .and("pf").as("pfData")
//                        .and(Aggregation.ROOT).as("users")
                        .and("empId").as("empId")
                        .and("sections.workingInformation.doj").as("doj")
                        .and("sections.workingInformation.dateOfRelieving").as("dol")
                        .and("sections.workingInformation.manpowerOutsourcing").as("manpowerOutsourcing")
                        .and("sections.workingInformation.payrollStatus").as("payrollStatus")
                        .and("sections.basicDetails.dob").as("dob")
                        .and("sections.basicDetails.gender").as("gender")
                        .and("payrollDetails").as("payrollDetails")
        );
//        Aggregation aggregation = Aggregation.newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, "userinfo", PayrollPaysheetDTO.class).getMappedResults();
    }



    private LookupOperation buildSimpleLookup(String from, String alias, String localField, String foreignField, Map<String, String> fieldFilters) {
        Criteria criteria = new Criteria();
        fieldFilters.forEach((field, value) -> {
            criteria.and(field).is(value);
        });

        // Add $expr condition for empId
        Criteria exprCriteria = Criteria.expr(
                ComparisonOperators.Eq.valueOf("$"+localField).equalToValue("$$letField")
        );

        return LookupOperation.newLookup()
                .from(from)
                .let(VariableOperators.Let.ExpressionVariable.newVariable("letField").forField(foreignField))
                .pipeline(
                        AggregationPipeline.of(
                               Aggregation.match(criteria),
                                Aggregation.match(exprCriteria)
                        )
                )
                .as(alias);
    }
    private LookupOperation buildLookupWithSortLimit(String from, String alias, int limit, String sortField) {
        return LookupOperation.newLookup()
                .from(from)
                .let(VariableOperators.Let.ExpressionVariable.newVariable("empId").forField("empId"))
                .pipeline(
                        AggregationPipeline.of(

                                Aggregation.match(
                                        Criteria.expr(
                                                ComparisonOperators.Eq.valueOf("$empId").equalToValue("$$empId")
                                        )
                                ),
                                Aggregation.limit(limit),
                                Aggregation.sort(Sort.by(Sort.Direction.DESC, sortField))
                        )
                )
                .as(alias);
    }




}
