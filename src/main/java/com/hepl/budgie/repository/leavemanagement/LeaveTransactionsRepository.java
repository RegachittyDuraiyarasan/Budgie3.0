package com.hepl.budgie.repository.leavemanagement;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;

@Repository
public interface LeaveTransactionsRepository extends MongoRepository<LeaveTransactions, String> {

    public static final String COLLECTION_NAME = "leave_transaction_type";
    
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default List<LeaveTransactions> fetchTransactions(String orgId, MongoTemplate mongoTemplate){

        String collection = getCollectionName(orgId);
        return mongoTemplate.findAll(LeaveTransactions.class, collection);
    }

}
