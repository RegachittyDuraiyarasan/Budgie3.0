package com.hepl.budgie.repository.probationProcess;

import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.probation.ProbationProcess;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProbationProcessRepository extends MongoRepository<ProbationProcess, String> {

    public static final String COLLECTION_NAME = "probation_process";

    default void saveProbation(ProbationProcess probationProcess, String org, MongoTemplate mongoTemplate) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        Query query = new Query(Criteria.where("empId").in(probationProcess.getEmpId()));
        Update update =  updateProbationProcess(probationProcess);
        mongoTemplate.upsert(query,update, collectionName);

    }

    default ProbationProcess findByEmpId(String empId, String org, MongoTemplate mongoTemplate) {

        if (StringUtils.isBlank(empId)) {
            return null;
        }
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        Query query = new Query(Criteria.where("empId").is(empId));
        return mongoTemplate.findOne(query, ProbationProcess.class, collectionName);
    }

    static Update updateProbationProcess(ProbationProcess dto){
        return new Update()
                .set("empId", dto.getEmpId())
                .set("reportingManagerId", dto.getReportingManagerId())
                .set("reportingManagerRatings", dto.getReportingManagerRatings())
                .set("status", dto.getStatus())
                .set("reportingManagerRemarks", dto.getReportingManagerRemarks())
                .set("extendedMonths",dto.getExtendedMonths())
                .set("extendedStatus", dto.getExtendedStatus())
                .set("finalRemarks" , dto.getFinalRemarks())
                .set("results", dto.getResults())
                .set("overAllRating", dto.getOverAllRating())
                .set("hrVerifyStatus", dto.getHrVerifyStatus())
                .set("followUps", dto.getFollowUps())
                .set("_class", ProbationProcess.class.getName());

    }

    default void updateField(MongoTemplate mongoTemplate, String orgId,String empId, ProbationProcess request) {
        Query query = new Query(Criteria.where("empId").is(empId));
        Update update = new Update();

        if (request.getHrVerifyStatus() != null) {
            update.set("hrVerifyStatus",request.getHrVerifyStatus());
        }
        if (request.getExtendedHrVerifyStatus() != null) {
            update.set("extendedHrVerifyStatus",request.getExtendedHrVerifyStatus());
        }

        mongoTemplate.upsert(query, update, ProbationProcess.class, COLLECTION_NAME +"_"+ orgId);
    }


}
