package com.hepl.budgie.repository.documentinfo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators.DateToString;
import org.springframework.data.mongodb.core.aggregation.DateOperators.Timezone;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.aggregation.StringOperators.Concat;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;

@Repository
public interface DocumentInfoRepo extends MongoRepository<DocumentInfo, String> {
        public final String COLLECTION_NAME = "t_emp_doc_center";

        Optional<DocumentInfo> findByEmpId(String empId);

        default List<ResponseDocumentDTO> getDocumentsWithUserInfo(MongoTemplate mongoTemplate, String org) {
                LookupOperation lookupOperation = LookupOperation.newLookup()
                                .from("userinfo")
                                .localField("empId")
                                .foreignField("empId")
                                .pipeline(AggregationPipeline.of(Aggregation.match(Criteria.expr(ComparisonOperators.Eq
                                                .valueOf("sections.workingInformation.payrollStatus")
                                                .equalToValue(org)))))
                                .as("userinfos");
                MatchOperation statusMatch = Aggregation.match(
                                Criteria.where("docdetails.status").ne("Deleted") // Exclude 'Deleted' status
                );
                StringOperators.Concat concat = StringOperators.Concat
                                .valueOf("userinfos.sections.basicDetails.firstName")
                                .concat(" ").concatValueOf("userinfos.sections.basicDetails.lastName");

                ProjectionOperation project = Aggregation.project()
                                .andInclude("empId")
                                .and(concat).as("userName")
                                .and("docdetails.moduleId").as("moduleId")
                                .and("docdetails.documentCategory").as("documentCategory")
                                .and("docdetails.title").as("title")
                                .and("docdetails.description").as("description")
                                .and("docdetails.acknowledgedType").as("acknowledgedType")
                                .and("docdetails.acknowledgementHeading").as("acknowledgementHeading")
                                .and("docdetails.acknowledgementDescription").as("acknowledgementDescription")
                                .and("docdetails.status").as("status")
                                .and(DateOperators.DateToString
                                                .dateToString("$docdetails.createdDate")
                                                .toString("%Y-%m-%d %H:%M:%S")
                                                .withTimezone(Timezone.valueOf("Asia/Kolkata")))
                                .as("createdDate")
                                .and(DateOperators.DateToString
                                                .dateToString("$docdetails.lastModifiedDate")
                                                .toString("%Y-%m-%d %H:%M:%S")
                                                .withTimezone(Timezone.valueOf("Asia/Kolkata")))
                                .as("lastModifiedDate")
                                .and("docdetails.fileDetails.folderName").as("folderName")
                                .and("docdetails.fileDetails.fileName").as("fileName");
                Aggregation aggregation = Aggregation.newAggregation(lookupOperation, Aggregation.unwind("userinfos"),
                                Aggregation.unwind("docdetails"), statusMatch, project);
                AggregationResults<ResponseDocumentDTO> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                ResponseDocumentDTO.class);
                return results.getMappedResults();
        }

}
