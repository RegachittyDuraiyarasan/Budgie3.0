package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.payroll.*;
import com.hepl.budgie.entity.payroll.PayrollReimbursementClaim;
import com.hepl.budgie.entity.payroll.ReimbursementBill;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public interface PayrollReimbursementClaimRepository extends MongoRepository<PayrollReimbursementClaim, String> {

        final String COLLECTION_NAME = "payroll_t_reimbursement_claims_";

        default Optional<PayrollReimbursementClaim> findByPayrollMonthAndEmpId(String payrollMonth, String empId,
                        String finYear, String orgId, MongoTemplate mongoTemplate) {
                Query query = new Query();
                query.addCriteria(Criteria.where("empId").is(empId).and("finYear").is(finYear).and("payrollMonth")
                                .is(payrollMonth));

                return Optional.ofNullable(
                                mongoTemplate.findOne(query, PayrollReimbursementClaim.class, COLLECTION_NAME + orgId));
        }

        default void saveReimbursementBill(PayrollReimbursementClaim claim, MongoTemplate mongoTemplate, String orgId) {
                mongoTemplate.save(claim, COLLECTION_NAME + orgId);
        }

        default List<ReimbursementBillDTO> listBills(String empId, MongoTemplate mongoTemplate, String finYear,
                        String orgId) {
                Criteria criteria = Criteria.where("finYear").is(finYear);
                if (empId != null) {
                        criteria = criteria.and("empId").is(empId);
                }

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(criteria), // Match empId
                                Aggregation.unwind("reimbursement", true),
                                Aggregation.unwind("reimbursement.reimbursementBills", true),
                                Aggregation.project()
                                                .and("empId").as("empId")
                                                .and("payrollMonth").as("payrollMonth")
                                                .and("finYear").as("finYear")
                                                .and("reimbursement.reimbursementBills.reimbursementId")
                                                .as("reimbursementId")
                                                .and("reimbursement.reimbursementBills.billAmount").as("billAmount")
                                                .and("reimbursement.reimbursementBills.approvedBillAmount")
                                                .as("approvedBillAmount")
                                                .and("reimbursement.reimbursementBills.billDate").as("billDate")
                                                .and("reimbursement.reimbursementBills.billNo").as("billNo")
                                                .and("reimbursement.reimbursementBills.claimDate").as("claimDate")
                                                .and("reimbursement.reimbursementBills.attachment").as("attachment")
                                                .and("reimbursement.reimbursementBills.status").as("status")
                                                .and("reimbursement.reimbursementBills.remarks").as("remarks")
                                                .and("reimbursement.fbpType").as("reimbursementType"),
                                Aggregation.sort(Sort.by(Sort.Direction.DESC, "payrollMonth")));

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME + orgId, ReimbursementBillDTO.class)
                                .getMappedResults();
        }

        default Optional<ReimbursementBillDTO> latestBills(MongoTemplate mongoTemplate, String finYear, String orgId) {
                Criteria criteria = Criteria.where("finYear").is(finYear);

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(criteria),
                                Aggregation.unwind("reimbursement", true),
                                Aggregation.unwind("reimbursement.reimbursementBills", true),
                                Aggregation.project()
                                                .and("empId").as("empId")
                                                .and("payrollMonth").as("payrollMonth")
                                                .and("finYear").as("finYear")
                                                .and("reimbursement.reimbursementBills.reimbursementId")
                                                .as("reimbursementId")
                                                .and("reimbursement.reimbursementBills.billAmount").as("billAmount")
                                                .and("reimbursement.reimbursementBills.approvedBillAmount")
                                                .as("approvedBillAmount")
                                                .and("reimbursement.reimbursementBills.billDate").as("billDate")
                                                .and("reimbursement.reimbursementBills.billNo").as("billNo")
                                                .and("reimbursement.reimbursementBills.claimDate").as("claimDate")
                                                .and("reimbursement.reimbursementBills.attachment").as("attachment")
                                                .and("reimbursement.reimbursementBills.status").as("status")
                                                .and("reimbursement.fbpType").as("reimbursementType"),
                                Aggregation.sort(Sort.by(Sort.Direction.DESC, "payrollMonth")),
                                Aggregation.limit(1));

                List<ReimbursementBillDTO> results = mongoTemplate
                                .aggregate(aggregation, COLLECTION_NAME + orgId, ReimbursementBillDTO.class)
                                .getMappedResults();
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        default boolean existsBills(String id, MongoTemplate mongoTemplate, String empId, PayrollMonth payrollMonth,
                        String orgId) {
                Query checkExistsQuery = new Query();
                checkExistsQuery.addCriteria(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.reimbursementBills.reimbursementId").is(id));

                // Check if reimbursementId exists
                return !mongoTemplate.exists(checkExistsQuery, COLLECTION_NAME + orgId);
        }

        default Optional<PayrollReimbursementClaim> existsBills(MongoTemplate mongoTemplate, String empId,
                        PayrollMonth payrollMonth, String orgId) {
                Query checkExistsQuery = new Query();
                checkExistsQuery.addCriteria(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear()));

                // Check if reimbursementId exists
                return Optional.ofNullable(mongoTemplate.findOne(checkExistsQuery, PayrollReimbursementClaim.class,
                                COLLECTION_NAME + orgId));
        }

        default BulkWriteResult bulkUpsert(List<PayrollFBPCreatePlan> dtoList, PayrollMonth payrollMonth,
                        MongoTemplate mongoTemplate, String orgId) {
                BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                                PayrollReimbursementClaim.class, COLLECTION_NAME + orgId);
                for (PayrollFBPCreatePlan dto : dtoList) {
                        Query query = new Query(Criteria.where("empId").is(dto.getEmpId())
                                        .and("payrollMonth").is(payrollMonth.getPayrollMonth()).and("finYear")
                                        .is(payrollMonth.getFinYear()));
                        Update update = buildUpdateFromDTO(dto, payrollMonth);
                        bulkOperations.upsert(query, update);
                }
                return bulkOperations.execute();
        }

        static Update buildUpdateFromDTO(PayrollFBPCreatePlan dto, PayrollMonth payrollMonth) {
                return new Update()
                                .set("empId", dto.getEmpId())
                                .set("payrollMonth", payrollMonth.getPayrollMonth())
                                .set("finYear", payrollMonth.getFinYear())
                                .set("endDate", AppUtils.parseLocalDate(dto.getEndDate(),
                                                LocaleContextHolder.getTimeZone().getID()));
        }

        default void approveOrRejectReimbursementBills(String id, ReimbursementApprovedDTO request,
                        MongoTemplate mongoTemplate, String empId, PayrollMonth payrollMonth, String orgId) {
                Query query = new Query(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.reimbursementBills.reimbursementId").is(id)

                );
                String status = request.getStatus() == 1 ? DataOperations.APPROVED.label
                                : DataOperations.REJECTED.label;
                Update update = new Update().set("reimbursement.$[].reimbursementBills.$[bill].status", status)
                                .set("reimbursement.$[].reimbursementBills.$[bill].approvedBillAmount",
                                                request.getApprovedBillAmount())
                                .filterArray("bill.reimbursementId", id);
                if (request.getStatus() == 0 && !request.getRemark().isEmpty() && !request.getRemark().isBlank()) {
                        update.set("reimbursement.$[].reimbursementBills.$[bill].remarks", request.getRemark());
                }
                mongoTemplate.updateFirst(query, update, COLLECTION_NAME + orgId);
        }

        default void deleteBills(String id, MongoTemplate mongoTemplate, String empId, PayrollMonth payrollMonth,
                        String orgId) {
                Query query = new Query(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.reimbursementBills.reimbursementId").is(id));

                Update update = new Update().pull("reimbursement.$[].reimbursementBills",
                                new Document("reimbursementId", id));

                mongoTemplate.updateFirst(query, update, COLLECTION_NAME + orgId);

                Query removeEmptyFbpTypeQuery = new Query(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.reimbursementBills").size(0));

                Update removeEmptyFbpTypeUpdate = new Update().pull("reimbursement",
                                new Document("reimbursementBills", new Document("$size", 0)));

                mongoTemplate.updateFirst(removeEmptyFbpTypeQuery, removeEmptyFbpTypeUpdate, COLLECTION_NAME + orgId);

                Query deleteIfNoReimbursement = new Query(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement").size(0));

                mongoTemplate.remove(deleteIfNoReimbursement, COLLECTION_NAME + orgId);
        }

        default void updateBills(String id, UpdateReimbursementDTO request,
                        ReimbursementBill.ReimbursementDocument document, MongoTemplate mongoTemplate, String empId,
                        PayrollMonth payrollMonth, String orgId) {
                Query checkExistsQuery = new Query(Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.reimbursementBills.reimbursementId").is(id));

                Update update = new Update()
                                .set("reimbursement.$[].reimbursementBills.$[bill].fbpType", request.getFbpType())
                                .set("reimbursement.$[].reimbursementBills.$[bill].billDate", request.getBillDate())
                                .set("reimbursement.$[].reimbursementBills.$[bill].billNo", request.getBillNo())
                                .set("reimbursement.$[].reimbursementBills.$[bill].billAmount", request.getBillAmount())
                                .set("reimbursement.$[].reimbursementBills.$[bill].attachment", new Document()
                                                .append("folderName", document.getFolderName())
                                                .append("fileName", document.getFileName()))
                                .filterArray("bill.reimbursementId", id);

                mongoTemplate.updateFirst(checkExistsQuery, update, COLLECTION_NAME + orgId);
        }

        default boolean existsBillNo(String id, UpdateReimbursementDTO request, MongoTemplate mongoTemplate,
                        String empId, PayrollMonth payrollMonth, String orgId) {
                Criteria criteria = Criteria.where("empId").is(empId)
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear())
                                .and("reimbursement.fbpType").is(request.getFbpType())
                                .and("reimbursement.reimbursementBills.billNo").is(request.getBillNo());

                Query query = new Query(criteria);
                return mongoTemplate.exists(query, COLLECTION_NAME + orgId);
        }

        default boolean updateExtendedDate(PayrollReimbursementClaim request, MongoTemplate mongoTemplate,
                        PayrollMonth payrollMonth, String orgId) {
                Query query = new Query(Criteria.where("empId").is(request.getEmpId())
                                .and("payrollMonth").is(payrollMonth.getPayrollMonth())
                                .and("finYear").is(payrollMonth.getFinYear()));

                Update update = new Update().set("endDate", request.getEndDate());

                UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollReimbursementClaim.class,
                                COLLECTION_NAME + orgId);
                return result.wasAcknowledged();
        }

        @SuppressWarnings("unchecked")
        default List<Map<String, Object>> getPendingReimbursementBills(MongoTemplate mongoTemplate, String orgId) {
                String collection = COLLECTION_NAME + orgId;

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.unwind("reimbursement"),
                                Aggregation.unwind("reimbursement.reimbursementBills"),
                                Aggregation.match(Criteria.where("reimbursement.reimbursementBills.status")
                                                .is(DataOperations.PENDING.label)),
                                Aggregation.project()
                                                .and("empId").as("empId")
                                                .and("payrollMonth").as("payrollMonth")
                                                .and("finYear").as("finYear")
                                                .and("reimbursement.reimbursementBills.reimbursementId")
                                                .as("reimbursementId")
                                                .and("reimbursement.fbpType").as("reimbursementType")
                                                .and("reimbursement.reimbursementBills.billAmount").as("billAmount")
                                                .and("reimbursement.reimbursementBills.approvedBillAmount")
                                                .as("approvedBillAmount")
                                                .and("reimbursement.reimbursementBills.billDate").as("billDate")
                                                .and("reimbursement.reimbursementBills.billNo").as("billNo")
                                                .and("reimbursement.reimbursementBills.claimDate").as("claimDate")
                                                .and("reimbursement.reimbursementBills.status").as("status")
                                                .and("reimbursement.reimbursementBills.remarks").as("remarks")
                                                .and("reimbursement.reimbursementBills.attachment").as("attachment"));

                AggregationResults<Map> results = mongoTemplate.aggregate(
                                aggregation,
                                collection,
                                Map.class);

                return results.getMappedResults().stream()
                                .map(item -> (Map<String, Object>) item)
                                .collect(Collectors.toList());
        }

}
