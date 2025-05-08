package com.hepl.budgie.entity.scheduler;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "job_execution_log")
@Builder
public class JobExecutionLog {

    @Id
    private String id;
    private String jobName;
    private LocalDateTime executionTime;
    private String status; // "SUCCESS" or "FAILURE"
    private int retryCount;
    private String errorMessage;

}
