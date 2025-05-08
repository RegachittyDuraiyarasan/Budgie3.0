package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.entity.leavemanagement.LeaveEncashment;
import com.hepl.budgie.repository.leavemanagement.LeaveEncashmentRepository;
import com.hepl.budgie.service.leavemanagement.LeaveEncashmentService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LeaveEncashmentServiceImpl implements LeaveEncashmentService {
    private final LeaveEncashmentRepository leaveEncashmentRepository;
    private final MongoTemplate mongoTemplate;

    public LeaveEncashmentServiceImpl(LeaveEncashmentRepository leaveEncashmentRepository, MongoTemplate mongoTemplate) {
        this.leaveEncashmentRepository = leaveEncashmentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<LeaveEncashment> getLeaveEncashmentList() {
        List<LeaveEncashment> encashmentList=leaveEncashmentRepository.findByStatus("Pending");
        return encashmentList;
    }
}
