package com.hepl.budgie.repository.leavemanagement;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.leave.LeaveApply;

public interface LeaveCalendarAdminRepository extends MongoRepository<LeaveApply, String> {
}
