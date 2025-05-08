package com.hepl.budgie.entity.scheduler;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;
import com.hepl.budgie.entity.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Document("scheduled_job")
public class ScheduledJob extends AuditInfo {

    @Id
    private String id;
    private String jobName;
    private String jobGroup;
    private String action;
    private Map<String, Object> parameters;
    @Builder.Default
    private String status = Status.ACTIVE.label;
    private String cronExpression;

}
