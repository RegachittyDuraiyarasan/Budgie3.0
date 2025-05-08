package com.hepl.budgie.repository.separation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import com.hepl.budgie.dto.separation.EmployeeSeparationDTO;
import com.hepl.budgie.dto.separation.SeparationReportDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.separation.Level;
import com.hepl.budgie.entity.separation.SeparationInfo;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.utils.MongoExpressionHelper;

@Repository
public interface SeparationRepository extends MongoRepository<SeparationInfo, String> {

        String COLLECTION_NAME = "separationInfo";
        String WITHDRAWAN = "Withdrawn";
        int minDays = 0;
        int maxDays = 7;

        default String getCollectionName(String org) {
                return org == null || org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
        }

        default EmployeeSeparationDTO updateOrInsertEmployeeSeparation(String org, EmployeeSeparationDTO dto,
                        MongoTemplate mongoTemplate) {
                String collectionName = getCollectionName(org);
                if (dto.getId() == null || dto.getId().isEmpty()) {
                        dto.setId(new ObjectId().toHexString());
                }
                Query query = new Query(Criteria.where("_id").is(dto.getId()));
                Update update = new Update()
                                .setOnInsert("empId", dto.getEmpId())
                                .setOnInsert("isSeparationBankAccNew", dto.getIsSeparationBankAccNew())
                                .setOnInsert("separationBankAccDetails", dto.getSeparationBankAccDetails())
                                .setOnInsert("reason", dto.getReason());
                if (dto.getRelievingDate() != null) {
                        update.set("relievingDate", dto.getRelievingDate());
                }
                update.setOnInsert("employeeRemarks", dto.getEmployeeRemarks());

                if (dto.getReportingManagerInfo() != null) {
                        update.set("reportingManagerInfo", dto.getReportingManagerInfo());
                }

                if (dto.getReviewerInfo() != null) {
                        update.set("reviewerInfo", dto.getReviewerInfo());
                }
                if (dto.getItInfraInfo() != null) {
                        update.set("itInfraInfo", dto.getItInfraInfo());
                }
                if (dto.getFinanceInfo() != null) {
                        update.set("financeInfo", dto.getFinanceInfo());
                }
                if (dto.getSiteAdminInfo() != null) {
                        update.set("siteAdminInfo", dto.getSiteAdminInfo());
                }
                if (dto.getRelivingDocumentInfo() != null) {
                        update.set("relivingDocumentInfo", dto.getRelivingDocumentInfo());
                }
                if (dto.getHrInfo() != null) {
                        update.set("hrInfo", dto.getHrInfo());
                }
                if (dto.getAccountInfoStatus() != null) {
                        update.set("accountInfoStatus", dto.getAccountInfoStatus());
                }

                update.set("updatedAt", LocalDateTime.now())
                                .setOnInsert("createdAt", LocalDateTime.now())
                                .setOnInsert("appliedDate",
                                                dto.getAppliedDate() != null ? dto.getAppliedDate()
                                                                : LocalDateTime.now())
                                .set("resignationStatus",
                                                dto.getResignationStatus() != null ? dto.getResignationStatus()
                                                                : "Pending")
                                .setOnInsert("noDueStatus",
                                                dto.getNoDueStatus() != null ? dto.getNoDueStatus() : "Pending");
                mongoTemplate.upsert(query, update, collectionName);
                return mongoTemplate.findOne(query, EmployeeSeparationDTO.class, collectionName);
        }

        default List<EmployeeSeparationDTO> getSeparationData(String org, String empId, MongoTemplate mongoTemplate) {
                String collectionName = getCollectionName(org);
                Query query = new Query(Criteria.where("empId").is(empId)
                                .orOperator(Criteria.where("resignationStatus").in(Status.PENDING.label),
                                                Criteria.where("resignationStatus").is(Status.COMPLETED.label)));
                return mongoTemplate.find(query, EmployeeSeparationDTO.class, collectionName);
        }

        default List<EmployeeSeparationDTO> getSeparationDataByRepoAndReview(
                        String org, String empId, String status, String level,
                        List<UserInfo> userInfos, MongoTemplate mongoTemplate) {

                String collectionName = getCollectionName(org);

                Criteria criteria;
                LocalDateTime currentDate = LocalDateTime.now();
                LocalDateTime oneMonthBefore = currentDate.plusMonths(1);

                if (Level.ITINFRA.label.equals(level)) {
                        criteria = Criteria.where("relievingDate").gte(oneMonthBefore).lte(currentDate);

                        if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                criteria = criteria.and("iTInfraInfo.status").is(Status.APPROVED.label);
                        } else {
                                criteria = new Criteria().andOperator(
                                                criteria,
                                                new Criteria().orOperator(
                                                                Criteria.where("iTInfraInfo").exists(false),
                                                                Criteria.where("iTInfraInfo").is(null),
                                                                Criteria.where("reportingManagerInfo.status")
                                                                                .is(Status.APPROVED.label)));
                        }
                } else {
                        if (userInfos == null || userInfos.isEmpty()) {
                                return List.of();
                        }

                        List<String> empIds = userInfos.stream().map(UserInfo::getEmpId).toList();
                        criteria = Criteria.where("empId").in(empIds);

                        if (Level.REPORTINGMANAGER.label.equals(level)) {
                                if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                        criteria = criteria.and("reportingManagerInfo.status")
                                                        .is(Status.APPROVED.label);
                                } else if ("Pending".equalsIgnoreCase(status)) {
                                        criteria = criteria.andOperator(
                                                        Criteria.where("reportingManagerInfo").is(null),
                                                        Criteria.where("reportingManagerInfo").exists(false),
                                                        Criteria.where("resignationStatus").is(status));

                                } else if (WITHDRAWAN.equalsIgnoreCase(status)) {
                                        criteria = criteria.and("resignationStatus").is(status);
                                }
                        } else if (Level.REVIEWER.label.equals(level)) {
                                if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                        criteria = criteria.and("reviewerInfo.status").is(Status.APPROVED.label);
                                } else if ("Pending".equalsIgnoreCase(status)) {
                                        criteria = new Criteria().andOperator(
                                                        Criteria.where("reportingManagerInfo.status")
                                                                        .is(Status.APPROVED.label),
                                                        Criteria.where("resignationStatus").is(status),
                                                        Criteria.where("reviewerInfo.status")
                                                                        .ne(Status.APPROVED.label));
                                } else if (WITHDRAWAN.equalsIgnoreCase(status)) {
                                        criteria = criteria.and("resignationStatus").is(status);
                                } else {
                                        criteria = criteria.andOperator(
                                                        criteria,
                                                        new Criteria().orOperator(
                                                                        Criteria.where("reportingManagerInfo")
                                                                                        .exists(false),
                                                                        Criteria.where("reportingManagerInfo").is(null),
                                                                        Criteria.where("reportingManagerInfo.status")
                                                                                        .is(Status.APPROVED.label))
                                                                        .andOperator(Criteria.where("reviewerInfo")
                                                                                        .is(null)),
                                                        Criteria.where("resignationStatus")
                                                                        .ne(WITHDRAWAN));
                                }
                        }
                }

                MatchOperation matchStage = Aggregation.match(criteria);

                LookupOperation lookupBasicDetails = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .as("basicDetails");

                UnwindOperation unwindBasicDetails = Aggregation.unwind("basicDetails", true);

                ProjectionOperation projectStage = Aggregation.project()
                                .and("_id").as("_id")
                                .and("empId").as("empId")
                                .and("isSeparationBankAccNew").as("isSeparationBankAccNew")
                                .and("separationBankAccDetails").as("separationBankAccDetails")
                                .and("reason").as("reason")
                                .and("employeeRemarks").as("employeeRemarks")
                                .and("reportingManagerInfo").as("reportingManagerInfo")
                                .and("reviewerInfo").as("reviewerInfo")
                                .and("itInfraInfo").as("itInfraInfo")
                                .and("financeInfo").as("financeInfo")
                                .and("siteAdminInfo").as("siteAdminInfo")
                                .and("accountInfoStatus").as("accountInfoStatus")
                                .and("relivingDocumentInfo").as("relivingDocumentInfo")
                                .and("hrInfo").as("hrInfo")
                                .and("noDueStatus").as("noDueStatus")
                                .and("resignationStatus").as("resignationStatus")
                                .and(DateOperators.DateToString
                                                .dateOf("appliedDate")
                                                .toString("%Y-%m-%d")
                                                .withTimezone(Timezone.valueOf("Asia/Kolkata")))
                                .as("appliedDate")
                                .and("basicDetails._id").as("basicInfoDto._id")
                                .and("basicDetails.empId").as("basicInfoDto.empId")
                                .and("basicDetails.sections.basicDetails.firstName").as("basicInfoDto.firstName")
                                .and("basicDetails.sections.basicDetails.lastName").as("basicInfoDto.lastName")
                                .and("basicDetails.sections.workingInformation.designation")
                                .as("basicInfoDto.designation")
                                .and(DateOperators.DateToString
                                                .dateOf("basicDetails.sections.workingInformation.doj")
                                                .toString("%Y-%m-%d")
                                                .withTimezone(Timezone.valueOf("Asia/Kolkata")))
                                .as("basicInfoDto.joiningDate")
                                .and("basicDetails.sections.hrInformation.noticePeriod").as("basicInfoDto.noticePeriod")
                                .and("basicDetails.sections.workingInformation.department")
                                .as("basicInfoDto.department");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchStage,
                                lookupBasicDetails,
                                unwindBasicDetails,
                                projectStage);

                AggregationResults<EmployeeSeparationDTO> results = mongoTemplate.aggregate(aggregation, collectionName,
                                EmployeeSeparationDTO.class);

                return results.getMappedResults();
        }

        default List<EmployeeSeparationDTO> getITInfraSeparationData(
                        String org, String level, String status,
                        MongoTemplate mongoTemplate) {

                String collectionName = getCollectionName(org);

                ProjectionOperation projectDays = Aggregation.project()
                                .andInclude("_id", "empId", "isSeparationBankAccNew", "separationBankAccDetails",
                                                "reason", "employeeRemarks", "reportingManagerInfo", "reviewerInfo",
                                                "accountInfoStatus",
                                                "itInfraInfo", "financeInfo", "siteAdminInfo", "relivingDocumentInfo",
                                                "hrInfo", "noDueStatus", "appliedDate", "relievingDate",
                                                "resignationStatus")
                                .andExpression("{$dateDiff: {startDate: '$$NOW', endDate: '$relievingDate', unit: 'day'}}")
                                .as("days");

                Criteria criteria = new Criteria();

                if (level.equals(Level.ITINFRA.label)) {
                        if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                criteria = Criteria.where("itInfraInfo.status").is(Status.APPROVED.label);
                        } else {
                                criteria = new Criteria().andOperator(
                                                Criteria.where("days").gte(minDays).lte(maxDays),
                                                new Criteria().orOperator(
                                                                Criteria.where("itInfraInfo").exists(false),
                                                                Criteria.where("itInfraInfo").is(null),
                                                                Criteria.where("reportingManagerInfo.status")
                                                                                .is(Status.APPROVED.label))
                                                                .andOperator(Criteria.where("itInfraInfo.status")
                                                                                .ne(Status.APPROVED.label)));
                        }
                } else if (level.equals(Level.FINANCE.label)) {
                        if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                criteria = Criteria.where("financeInfo.status").is(Status.APPROVED.label);
                        } else {
                                criteria = new Criteria().andOperator(
                                                Criteria.where("days").gte(minDays).lte(maxDays),
                                                new Criteria().orOperator(
                                                                Criteria.where("financeInfo").exists(false),
                                                                Criteria.where("financeInfo").is(null),
                                                                Criteria.where("reportingManagerInfo.status")
                                                                                .is(Status.APPROVED.label))
                                                                .andOperator(Criteria.where("financeInfo.status")
                                                                                .ne(Status.APPROVED.label)));
                        }
                } else if (level.equals(Level.SITEADMIN.label)) {
                        if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                criteria = Criteria.where("siteAdminInfo.status").is(Status.APPROVED.label);
                        } else {
                                criteria = new Criteria().andOperator(
                                                Criteria.where("days").gte(minDays).lte(maxDays),
                                                new Criteria().orOperator(
                                                                Criteria.where("siteAdminInfo").exists(false),
                                                                Criteria.where("siteAdminInfo").is(null),
                                                                Criteria.where("reportingManagerInfo.status")
                                                                                .is(Status.APPROVED.label))
                                                                .andOperator(Criteria.where("siteAdminInfo.status")
                                                                                .ne(Status.APPROVED.label)));
                        }
                } else if (Level.ACCOUNT.label.equalsIgnoreCase(level)) {

                        if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                                criteria = new Criteria().andOperator(
                                                Criteria.where("isSeparationBankAccNew").is(true),
                                                Criteria.where("accountInfoStatus").is(Status.APPROVED.label));
                        } else {
                                criteria = new Criteria().andOperator(
                                                Criteria.where("isSeparationBankAccNew").is(true),
                                                // Criteria.where("isSeparationBankAccNew").nin("false"),
                                                Criteria.where("accountInfoStatus").is(null));
                        }

                }

                MatchOperation matchStage = Aggregation.match(criteria);

                MatchOperation matchValidEmpId = Aggregation.match(
                                Criteria.where("empId").ne(null).ne(""));

                LookupOperation lookupBasicDetails = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .as("basicDetails");

                UnwindOperation unwindBasicDetails = Aggregation.unwind("basicDetails", true);

                ProjectionOperation projectStage = Aggregation.project()
                                .andInclude("_id", "empId", "isSeparationBankAccNew", "separationBankAccDetails",
                                                "reason", "employeeRemarks", "reportingManagerInfo", "reviewerInfo",
                                                "accountInfoStatus",
                                                "itInfraInfo", "financeInfo", "siteAdminInfo", "relivingDocumentInfo",
                                                "hrInfo", "noDueStatus", "relievingDate", "resignationStatus")
                                .andExpression("{$dateToString: {format: '%Y-%m-%d', date: '$appliedDate', timezone: 'Asia/Kolkata'}}")
                                .as("appliedDate")
                                .and("basicDetails._id").as("basicInfoDto._id")
                                .and("basicDetails.empId").as("basicInfoDto.empId")
                                .and("basicDetails.sections.basicDetails.firstName").as("basicInfoDto.firstName")
                                .and("basicDetails.sections.basicDetails.lastName").as("basicInfoDto.lastName")
                                .and("basicDetails.sections.workingInformation.designation")
                                .as("basicInfoDto.designation")
                                .andExpression(
                                                "{$dateToString: {format: '%Y-%m-%d', date: '$basicDetails.sections.workingInformation.doj', timezone: 'Asia/Kolkata'}}")
                                .as("basicInfoDto.joiningDate")
                                .and("basicDetails.sections.hrInformation.noticePeriod").as("basicInfoDto.noticePeriod")
                                .and("basicDetails.sections.workingInformation.department")
                                .as("basicInfoDto.department");

                Aggregation aggregation = Aggregation.newAggregation(
                                projectDays,
                                matchStage,
                                matchValidEmpId,
                                lookupBasicDetails,
                                unwindBasicDetails,
                                projectStage);

                AggregationResults<EmployeeSeparationDTO> results = mongoTemplate.aggregate(
                                aggregation, collectionName, EmployeeSeparationDTO.class);

                return results.getMappedResults();
        }

        default List<EmployeeSeparationDTO> getHROpsSeparationData(
                        String org, String status, MongoTemplate mongoTemplate, TimeZone timeZone) {

                String collectionName = getCollectionName(org);

                ProjectionOperation projectDays = Aggregation.project()
                                .andInclude("_id", "empId", "isSeparationBankAccNew", "separationBankAccDetails",
                                                "reason", "employeeRemarks", "reportingManagerInfo", "reviewerInfo",
                                                "accountInfoStatus", "basicInfoDto",
                                                "itInfraInfo", "financeInfo", "siteAdminInfo", "relivingDocumentInfo",
                                                "hrInfo", "noDueStatus", "appliedDate", "relievingDate",
                                                "resignationStatus")
                                .andExpression("{$dateDiff: {startDate: '$$NOW', endDate: '$relievingDate', unit: 'day'}}")
                                .as("days");

                Criteria criteria = new Criteria();

                if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                        criteria = Criteria.where("hrInfo.status").is(Status.APPROVED.label);
                } else if (WITHDRAWAN.equalsIgnoreCase(status)) {
                        criteria = Criteria.where("resignationStatus").is(WITHDRAWAN);
                } else {
                        criteria = new Criteria().orOperator(
                                        Criteria.where("days").gte(minDays).lte(maxDays),
                                        Criteria.where("hrInfo").exists(false),
                                        Criteria.where("hrInfo").is(null))
                                        .andOperator(new Criteria().orOperator(
                                                        Criteria.where("reviewerInfo.status").is(Status.APPROVED.label)
                                        // Criteria.where("financeInfo.status").is(Status.APPROVED.label),
                                        // Criteria.where("siteAdminInfo.status")
                                        // .is(Status.APPROVED.label)
                                        ));
                }

                MatchOperation matchStage = Aggregation.match(criteria);

                MatchOperation matchValidEmpId = Aggregation.match(
                                Criteria.where("empId").ne(null).ne(""));

                LookupOperation lookupBasicDetails = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .as("basicDetails");

                UnwindOperation unwindBasicDetails = Aggregation.unwind("basicDetails", true);

                ProjectionOperation projectStage = Aggregation.project()
                                .andInclude("_id", "empId", "isSeparationBankAccNew", "separationBankAccDetails",
                                                "reason", "employeeRemarks", "reportingManagerInfo", "reviewerInfo",
                                                "accountInfoStatus",
                                                "itInfraInfo", "financeInfo", "siteAdminInfo", "relivingDocumentInfo",
                                                "hrInfo", "noDueStatus", "resignationStatus", "relievingDate")
                                .andExpression("{$dateToString: {format: '%Y-%m-%d', date: '$appliedDate', timezone: 'Asia/Kolkata'}}")
                                .as("appliedDate")
                                .and("basicDetails._id").as("basicInfoDto._id")
                                .and("basicDetails.empId").as("basicInfoDto.empId")
                                .and("basicDetails.sections.basicDetails.firstName").as("basicInfoDto.firstName")
                                .and("basicDetails.sections.basicDetails.lastName").as("basicInfoDto.lastName")
                                .and("basicDetails.sections.workingInformation.designation")
                                .as("basicInfoDto.designation")
                                .andExpression(
                                                "{$dateToString: {format: '%Y-%m-%d', date: '$basicDetails.sections.workingInformation.doj', timezone: 'Asia/Kolkata'}}")
                                .as("basicInfoDto.joiningDate")
                                .and("basicDetails.sections.hrInformation.noticePeriod").as("basicInfoDto.noticePeriod")
                                .and("basicDetails.sections.workingInformation.department")
                                .as("basicInfoDto.department")
                                .and(MongoExpressionHelper.dateToString("$relievingDate", "%Y-%m-%d", timeZone))
                                .as("basicInfoDto.relievingDate")
                                .and(MongoExpressionHelper.dateToString("$appliedDate", "%Y-%m-%d", timeZone))
                                .as("basicInfoDto.appliedDate");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchStage,
                                matchValidEmpId,
                                lookupBasicDetails,
                                unwindBasicDetails,
                                projectStage,
                                projectDays);

                AggregationResults<EmployeeSeparationDTO> results = mongoTemplate.aggregate(
                                aggregation, collectionName, EmployeeSeparationDTO.class);

                return results.getMappedResults();
        }

        default List<SeparationReportDTO> getHRSeparationReportData(
                        String org, String status, MongoTemplate mongoTemplate) {

                String collectionName = getCollectionName(org);

                Criteria criteria;
                if (Status.APPROVED.label.equalsIgnoreCase(status)) {
                        criteria = Criteria.where("hrInfo.status").is(Status.APPROVED.label);
                } else if (WITHDRAWAN.equalsIgnoreCase(status)) {
                        criteria = Criteria.where("resignationStatus").is(WITHDRAWAN);
                } else {
                        criteria = new Criteria().orOperator(
                                        Criteria.where("hrInfo").exists(false),
                                        Criteria.where("hrInfo").is(null))
                        // .andOperator(
                        // new Criteria().orOperator(
                        // Criteria.where("itInfraInfo.status")
                        // .is(Status.APPROVED.label),
                        // Criteria.where("financeInfo.status")
                        // .is(Status.APPROVED.label),
                        // Criteria.where("siteAdminInfo.status")
                        // .is(Status.APPROVED.label)))
                        ;
                }

                MatchOperation matchStage = Aggregation.match(criteria);

                MatchOperation matchValidEmpId = Aggregation.match(
                                Criteria.where("empId").ne(null).ne(""));

                LookupOperation lookupBasicDetails = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .as("basicDetails");

                UnwindOperation unwindBasicDetails = Aggregation.unwind("basicDetails", true);
                ProjectionOperation projectStage = Aggregation.project("_id")
                                .and("hrInfo.status").as("hrStatus")
                                .and("reportingManagerInfo.status").as("reportingManagerStatus")
                                .and("reviewerInfo.status").as("reviewerStatus")
                                .and("itInfraInfo.status").as("itInfraStatus")
                                .and("financeInfo.status").as("financeStatus")
                                .and("siteAdminInfo.status").as("siteAdminStatus")
                                .and("accountInfoStatus").as("accountInfoStatus")
                                .and("resignationStatus").as("employeeStatus")
                                .and("basicDetails._id").as("basicInfoDto._id")
                                .and("basicDetails.empId").as("basicInfoDto.empId")
                                .and("basicDetails.sections.basicDetails.firstName").as("basicInfoDto.firstName")
                                .and("basicDetails.sections.basicDetails.lastName").as("basicInfoDto.lastName")
                                .and("basicDetails.sections.workingInformation.designation")
                                .as("basicInfoDto.designation")
                                .andExpression(
                                                "{$dateToString: {format: '%Y-%m-%d', date: '$basicDetails.sections.workingInformation.doj', timezone: 'Asia/Kolkata'}}")
                                .as("basicInfoDto.joiningDate")
                                .and("basicDetails.sections.hrInformation.noticePeriod").as("basicInfoDto.noticePeriod")
                                .and("basicDetails.sections.workingInformation.department")
                                .as("basicInfoDto.department")
                                .and("appliedDate").as("basicInfoDto.appliedDate")
                                .and("relievingDate").as("basicInfoDto.relievingDate");

                Aggregation aggregation = Aggregation.newAggregation(
                                matchStage,
                                matchValidEmpId,
                                lookupBasicDetails,
                                unwindBasicDetails,
                                projectStage);

                AggregationResults<SeparationReportDTO> results = mongoTemplate.aggregate(
                                aggregation, collectionName, SeparationReportDTO.class);

                return results.getMappedResults();
        }

        default List<SeparationInfo> getUpcomingRelieving(List<String> orgCodes, MongoTemplate mongoTemplate) {
                LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));
                List<SeparationInfo> allResults = new ArrayList<>();

                for (String orgCode : orgCodes) {
                        String collectionName = "separationInfo_" + orgCode;

                        Aggregation aggregation = Aggregation.newAggregation(
                                        Aggregation.match(
                                                        Criteria.where("relievingDate")
                                                                        .gte(currentDate.atStartOfDay(
                                                                                        ZoneId.of("Asia/Kolkata"))
                                                                                        .toInstant())
                                                                        .lte(currentDate.plusDays(32)
                                                                                        .atStartOfDay(ZoneId.of(
                                                                                                        "Asia/Kolkata"))
                                                                                        .toInstant())),
                                        Aggregation.project()
                                                        .and("_id").as("id")
                                                        .and("empId").as("empId")
                                                        .and("resignationStatus").as("resignationStatus")
                                                        .and("relievingDate").as("relievingDate")
                                                        .andExpression("{ $dateDiff: { startDate: '$$NOW', endDate: '$relievingDate', unit: 'day' } }")
                                                        .as("daysUntilRelieving"));

                        allResults.addAll(
                                        mongoTemplate.aggregate(aggregation, collectionName, SeparationInfo.class)
                                                        .getMappedResults());
                }
                return allResults;
        }

}