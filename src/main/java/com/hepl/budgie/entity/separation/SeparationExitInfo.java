package com.hepl.budgie.entity.separation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.Data;

@Data
@Document(collection = "separation_exit_info") 
public class SeparationExitInfo extends AuditInfo {
    @Id
    private String id;
    private String separationId;
    private String empId;
    private LocalDateTime submittedOn;
    private String comments;
    private List<String> reasons;
    private Map<String, String> jobItself;
    private Map<String, String> remunerationAndBenefits;
    private Map<String, String> supervisorOrManager;
    private Map<String, String> company;
    private Map<String, String> management;
}