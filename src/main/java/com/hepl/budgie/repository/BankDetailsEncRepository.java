package com.hepl.budgie.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.userinfo.BankDetailsEnc;

public interface BankDetailsEncRepository extends MongoRepository<BankDetailsEnc, String> {

}
