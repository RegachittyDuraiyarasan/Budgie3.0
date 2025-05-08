package com.hepl.budgie.config.scheduler;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.scheduler.JobExecutionLog;
import com.hepl.budgie.entity.scheduler.ScheduledJob;
import com.hepl.budgie.repository.scheduler.JobExecutionLogRepository;
import com.hepl.budgie.repository.scheduler.ScheduledJobRepository;
import com.hepl.budgie.service.scheduler.JobAction;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobManager {

    private final ThreadPoolTaskScheduler scheduler;
    private final ScheduledJobRepository jobRepo;
    private final JobExecutionLogRepository jobExecutionLogRepository;
    private final ActionRegistry actionRegistry;
    private final MongoTemplate mongoTemplate;
    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadJobsFromDB() {
        List<ScheduledJob> jobs = jobRepo.findByStatus(Status.ACTIVE.label);
        for (ScheduledJob job : jobs) {
            scheduleJob(job);
        }
    }

    public void scheduleJob(ScheduledJob jobEntity) {
        Runnable task = () -> {
            boolean success = false;
            String errorMessage = "";
            try {
                JobAction action = actionRegistry.getAction(jobEntity.getAction());
                if (action != null) {
                    action.execute(jobEntity.getJobName(), jobEntity.getParameters());
                    success = true;
                } else {
                    log.error("Unknown action: " + jobEntity.getAction());
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                log.error("Job execution failed: " + e.getMessage());
            }

            JobExecutionLog logEntity = JobExecutionLog.builder().jobName(jobEntity.getJobName())
                    .status(success ? Status.SUCCESS.label : Status.FAILED.label).executionTime(LocalDateTime.now())
                    .errorMessage(errorMessage)
                    .retryCount(0)
                    .build();
            jobExecutionLogRepository.save(logEntity);
        };

        ScheduledFuture<?> future = scheduler.schedule(task, new CronTrigger(jobEntity.getCronExpression()));
        runningTasks.put(jobEntity.getJobName(), future);
    }

    public void createOrUpdateJob(String jobId, String cron, String action, Map<String, Object> parameters) {
        removeJob(jobId); // cancel if running

        ScheduledJob job = ScheduledJob.builder().action(action).cronExpression(cron).parameters(parameters)
                .status(Status.ACTIVE.label)
                .build();
        jobRepo.updateJonById(jobId, job, mongoTemplate);

        scheduleJob(job);
    }

    public void removeJob(String jobId) {
        ScheduledFuture<?> future = runningTasks.remove(jobId);
        if (future != null)
            future.cancel(false);
        jobRepo.findById(jobId).ifPresent(job -> {
            job.setStatus(Status.INACTIVE.label);
            jobRepo.save(job);
        });
    }

}
