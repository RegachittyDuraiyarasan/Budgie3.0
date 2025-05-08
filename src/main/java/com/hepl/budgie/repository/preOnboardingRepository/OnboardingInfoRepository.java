package com.hepl.budgie.repository.preOnboardingRepository;

import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface OnboardingInfoRepository extends MongoRepository<OnBoardingInfo, String> {
    Optional<OnBoardingInfo> findByEmpId(String empId);


    List<OnBoardingInfo> findAllByEmailRequestDetailsIsEmailCreationInitiated(boolean b);

    @Query("{'seatingRequestDetails.isSeatingRequestInitiated': ?0, 'seatingRequestDetails.isIdCardRequestInitiated': ?1}")
    List<OnBoardingInfo> findBySeatingRequestDetails_IsSeatingRequestInitiatedAndSeatingRequestDetails_IsIdCardRequestInitiated(boolean seatingRequest, boolean idCardRequest);

    List<OnBoardingInfo> findAllByEmailRequestDetailsIsEmailIdCreated(boolean b);}
