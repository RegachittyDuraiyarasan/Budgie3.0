package com.hepl.budgie.repository.scheduler;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.scheduler.ScheduledJob;

public interface ScheduledJobRepository extends MongoRepository<ScheduledJob, String> {

    List<ScheduledJob> findByStatus(String status);

    default void updateJonById(String id, ScheduledJob job, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();
        update.set("cronExpression", job.getCronExpression());
        update.set("action", job.getAction());
        update.set("parameters", job.getParameters());
        update.set("status", job.getStatus());

        mongoTemplate.updateFirst(query, update, ScheduledJob.class);
    }

}
