package com.hepl.budgie.repository.userinfo;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;

public interface UserOnboardingInfoRepository extends MongoRepository<OnBoardingInfo, String> {

    public static final String COLLECTION_NAME = "user_onboarding_info";

    default boolean getOnboardingStatus(String empId, MongoTemplate mongoTemplate) {
        Query query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
                Criteria.where("onboardingStatus").is(true)));

        return mongoTemplate.count(query, empId) > 0;
    }

}
