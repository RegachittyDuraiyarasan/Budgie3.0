package com.hepl.budgie.repository.ma;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.Users;

public interface UserMaRepository extends MongoRepository<Users, String> {

}
