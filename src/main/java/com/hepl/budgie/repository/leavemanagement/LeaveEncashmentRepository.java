package com.hepl.budgie.repository.leavemanagement;

import com.hepl.budgie.entity.leavemanagement.LeaveEncashment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LeaveEncashmentRepository extends MongoRepository<LeaveEncashment, String> {

    List<LeaveEncashment> findByStatus(String pending);
}
