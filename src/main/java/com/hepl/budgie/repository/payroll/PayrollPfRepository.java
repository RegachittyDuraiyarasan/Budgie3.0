package com.hepl.budgie.repository.payroll;

 import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
 import com.hepl.budgie.dto.payroll.PayrollPfDTO;
 import com.hepl.budgie.entity.Status;
 import com.hepl.budgie.entity.payroll.PayrollComponent;
 import com.hepl.budgie.entity.payroll.PayrollLwf;
 import com.hepl.budgie.entity.payroll.PayrollPf;
 import com.hepl.budgie.utils.AppUtils;
 import com.mongodb.client.result.UpdateResult;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

 import java.util.List;
 import java.util.Optional;
 import java.util.stream.Collectors;


public interface PayrollPfRepository extends MongoRepository<PayrollPf,String> {
     public static final String COLLECTION_NAME = "payroll_m_pf_";

    static Optional<PayrollPf> findBypfId(MongoTemplate mongoTemplate, String country, String lwfId){
        Query query = new Query(Criteria.where("pfId").is(lwfId).and("status").nin(Status.DELETED.label));
        PayrollPf result = mongoTemplate.findOne(query, PayrollPf.class, COLLECTION_NAME+country);
        return Optional.ofNullable(result);
    }
    static Optional<PayrollPf> findTopByOrderByIdDescpf(String country, MongoTemplate mongoTemplate){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        PayrollPf result = mongoTemplate.findOne(query, PayrollPf.class, COLLECTION_NAME+country);
        return Optional.ofNullable(result);
    }
     static boolean upsertPf(PayrollPfDTO dto, MongoTemplate mongoTemplate, String country, String id) {
         Query query = new Query();
//         System.out.println("-------------------------------"+dto.getPfId());
         Update update = new Update();

         if(id.isEmpty())
         {
             query.addCriteria(Criteria.where("pfId").is(dto.getPfId()));

             update.set("pfId", dto.getPfId());
         }else {
             query.addCriteria(Criteria.where("pfId").is(id));

         }

         update.set("pfName", dto.getPfName())
                  .set("contributionType", dto.getContributionType())
                  .set("percentage", dto.getPercentage())
                  .set("fixedAmount", dto.getFixedAmount())
                  .set("orgId", dto.getOrgId())
                  .set("component", dto.getComponent())
                  .set("minimumSalary", dto.getMinimumSalary())
                  .set("ceilingLimit", dto.getCeilingLimit())
                  .set("effectiveDate", dto.getEffectiveDate())
                  .set("description", dto.getDescription())
                  .set("status", dto.getStatus())
                  .set("employerContribution", dto.getEmployerContribution())
                  .set("employeeContribution", dto.getEmployeeContribution());
          UpdateResult result = mongoTemplate.upsert(query, update, PayrollPf.class, COLLECTION_NAME+country);
          System.out.println("Was Acknowledged: " + result.wasAcknowledged());
          return result.wasAcknowledged();
     }


    default boolean existsByStateAndOrgIdIn(MongoTemplate mongoTemplate, PayrollPfDTO dto, String country, String operation) {
        Query query = new Query();
         query.addCriteria(Criteria.where("orgId").in(dto.getOrgId()));
        query.addCriteria(Criteria.where("status").nin(Status.DELETED.label));
        if(operation.equalsIgnoreCase("Update")){
            query.addCriteria(Criteria.where("orgId").nin(dto.getOrgId()));
        }
        return mongoTemplate.exists(query,PayrollPf.class, COLLECTION_NAME+"_"+country);
    }

    static void deletePF(String id, MongoTemplate mongoTemplate, String country) {
        Query query = new Query(Criteria.where("pfId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        mongoTemplate.findAndModify(query, update, PayrollPf.class, COLLECTION_NAME + country);
    }

    static List<PayrollPf> findByOrgId(MongoTemplate mongoTemplate, String country, List<String> organization){
        Query query=new Query(Criteria.where("orgId").in(organization).
                and("status").is(Status.DELETED.label));
        return mongoTemplate.find(query,PayrollPf.class,COLLECTION_NAME +country);
    }

    static List<PayrollPf> findBypflist(MongoTemplate mongoTemplate,String organization){
        Query query=new Query(Criteria.where("status").is(Status.ACTIVE));
        query.fields().include("pfId").include("pfName")
                .include("percentage").include("contributionType").include("fixedAmount").include("status");
        return mongoTemplate.find(query,PayrollPf.class,COLLECTION_NAME +organization);
    }



}
