package com.hepl.budgie.repository.leavemanagement;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.leavemanagement.LeaveApply;

public interface LeaveApplyRepository extends MongoRepository<LeaveApply, String> {
    
    public static final String COLLECTION_NAME = "leave_apply";
    
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    LeaveApply findByLeaveCode(String leaveCode);
}
