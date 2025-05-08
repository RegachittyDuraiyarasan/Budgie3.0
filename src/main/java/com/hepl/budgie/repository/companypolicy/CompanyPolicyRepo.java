package com.hepl.budgie.repository.companypolicy;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.dto.companypolicy.CompanyPolicyDto;
import com.hepl.budgie.entity.companypolicy.CompanyDocDetails;
import com.hepl.budgie.entity.companypolicy.CompanyPolicy;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.organization.OrganizationMap;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.MongoTemplate;

@Repository
public interface CompanyPolicyRepo extends MongoRepository<CompanyPolicy,String> {
    
     public static final String COLLECTION_NAME = "t_company_policy_doc_center";

     Optional<CompanyPolicy> findTopByOrderByIdDesc();
   
     // default Optional<CompanyPolicy> findByPolicyCategory(String policyCategory,MongoTemplate mongoTemplate){
     //      Query query = new Query(Criteria.where("comDocDetails.policyCategory").is(policyCategory));
     //    return Optional.ofNullable(mongoTemplate.findOne(query, CompanyPolicy.class));
     // }
     default Optional<CompanyPolicy> findByPolicyCategory(String policyCategory, MongoTemplate mongoTemplate) {
          Query query = new Query(Criteria.where("comDocDetails").elemMatch(Criteria.where("policyCategory").is(policyCategory)));
          return Optional.ofNullable(mongoTemplate.findOne(query, CompanyPolicy.class));
      }
      
     
}
