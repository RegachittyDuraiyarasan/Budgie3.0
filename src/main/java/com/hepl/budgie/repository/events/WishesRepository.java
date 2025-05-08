package com.hepl.budgie.repository.events;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.event.Wishes;

public interface WishesRepository extends MongoRepository<Wishes, String> {
    
    public static final String COLLECTION_NAME = "t_wishes";
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }
}
