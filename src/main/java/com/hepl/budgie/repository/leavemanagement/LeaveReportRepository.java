package com.hepl.budgie.repository.leavemanagement;

import com.hepl.budgie.entity.leavemanagement.LeaveApply;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeaveReportRepository extends MongoRepository<LeaveApply, String> {
}
