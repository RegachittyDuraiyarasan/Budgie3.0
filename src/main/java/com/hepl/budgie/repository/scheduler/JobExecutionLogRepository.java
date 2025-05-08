package com.hepl.budgie.repository.scheduler;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.scheduler.JobExecutionLog;

public interface JobExecutionLogRepository extends MongoRepository<JobExecutionLog, String> {

}
