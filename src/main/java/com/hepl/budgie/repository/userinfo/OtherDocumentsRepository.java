package com.hepl.budgie.repository.userinfo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.userinfo.UserOtherDocuments;

import java.util.Optional;

public interface OtherDocumentsRepository extends MongoRepository<UserOtherDocuments,String> {
    Optional<UserOtherDocuments> findByEmpId (String empId);
}
