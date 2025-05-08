package com.hepl.budgie.repository.documentinfo;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.aggregation.VariableOperators;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let.ExpressionVariable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.documentInfo.DocumentCenterResponseReportDTo;
import com.hepl.budgie.dto.documentInfo.DocumentResponseReportDTO;
import com.hepl.budgie.entity.documentinfo.DocumentCenterReport;
import com.hepl.budgie.enums.DocumentCenterReportEnum;

@Repository
public interface DocumentCenterReportRepo extends
                MongoRepository<DocumentCenterReport, String> {
        public final String COLLECTION_NAME = "documentCenterReport";

        default void saveOrUpdate(DocumentCenterResponseReportDTo documentCenterReport, MongoTemplate mongoTemplate,
                        String empId) {

                List<Criteria> criterias = new ArrayList<>() {
                        {
                                add(Criteria.where("empId").is(empId));
                                add(Criteria.where("category").is(documentCenterReport.getCategory()));
                        }
                };

                Update update = new Update();
                update.setOnInsert("empId", empId);
                update.setOnInsert("process", documentCenterReport.getProcess());
                update.setOnInsert("category", documentCenterReport.getCategory());

                if (documentCenterReport.getStatus().equals(DocumentCenterReportEnum.ACCEPT.label)
                                || documentCenterReport.getStatus().equals(DocumentCenterReportEnum.DECLINE.label)) {
                        update.set("status", documentCenterReport.getStatus());
                        update.set("downloadStatus", "No");
                } else if (documentCenterReport.getStatus().equals(DocumentCenterReportEnum.DOWNLOAD.label)) {
                        criterias.add(Criteria.where("status").is(DocumentCenterReportEnum.ACCEPT.label));
                        update.set("status", DocumentCenterReportEnum.ACCEPT.label);
                        update.set("downloadStatus", "Yes");
                } else {
                        update.set("status", "Pending");
                        update.set("downloadStatus", "No");
                }
                Query query = new Query(new Criteria().andOperator(criterias));
                mongoTemplate.upsert(query, update, DocumentCenterReport.class);
        }

        default List<DocumentResponseReportDTO> getDocumentReport(String org, MongoTemplate mongoTemplate) {

                // Lookup from userinfo and filter by payrollStatus
                LookupOperation lookupOperationuser = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .pipeline(AggregationPipeline.of(Aggregation.match(Criteria.expr(
                                                ComparisonOperators.Eq
                                                                .valueOf("sections.workingInformation.payrollStatus")
                                                                .equalToValue(org)))))
                                .as("userDetail");
                StringOperators.Concat concat = StringOperators.Concat
                                .valueOf("userDetail.sections.basicDetails.firstName")
                                .concat(" ")
                                .concatValueOf("userDetail.sections.basicDetails.lastName");

                UnwindOperation userInfo = Aggregation.unwind("userDetail");

                LookupOperation lookupOperationemp = LookupOperation.newLookup()
                                .from("module_m_settings_ORG00001")
                                .let(ExpressionVariable.newVariable("categoryRef").forField("category"))
                                .pipeline(AggregationPipeline.of(
                                                Aggregation.match(Criteria.where("referenceName")
                                                                .is("Add Document Category")),
                                                Aggregation.unwind("options"),
                                                Aggregation.match(Criteria.expr(
                                                                ComparisonOperators.Eq.valueOf("$options.moduleId")
                                                                                .equalToValue("$$categoryRef")))))
                                .as("documentDetails");

                UnwindOperation empCenter = Aggregation.unwind("documentDetails");

                LookupOperation lookupOperationdocCenter = LookupOperation.newLookup()
                                .from("t_emp_doc_center")
                                .let(ExpressionVariable.newVariable("documenttitle").forField("category"))
                                .pipeline(AggregationPipeline.of(
                                                Aggregation.unwind("docdetails"),
                                                Aggregation.match(Criteria.expr(
                                                                ComparisonOperators.Eq.valueOf("$docdetails.moduleId")
                                                                                .equalToValue("$$documenttitle")))))
                                .as("emp_document");

                UnwindOperation empDocument = Aggregation.unwind("emp_document");
                ProjectionOperation empDocumentProjection = Aggregation.project()
                                .andInclude("empId")
                                .and(concat).as("userName")
                                .andInclude("process")
                                .andInclude("status")
                                .andInclude("downloadStatus")
                                .and("$documentDetails.options.documentType").as("documentCategory")
                                .and("$emp_document.docdetails.title").as("documentTitle");

                Aggregation aggregation = Aggregation.newAggregation(
                                lookupOperationuser,
                                userInfo,
                                lookupOperationemp, empCenter, lookupOperationdocCenter, empDocument,
                                empDocumentProjection);
                return mongoTemplate
                                .aggregate(aggregation, COLLECTION_NAME, DocumentResponseReportDTO.class)
                                .getMappedResults();
        }

}
