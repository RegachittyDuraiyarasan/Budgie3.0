package com.hepl.budgie.entity.pms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Document(collection = "pmslevel")
@AllArgsConstructor
@NoArgsConstructor
public class PmsLevel {
    @Id
    private String id;
    private String levelId;
    private String levelName;
    private String levelType;
    private int activeStatus = 1;
    private List<String> btnLabel;
    private List<String> action;
    private List<Integer> actionStatus;
    private ZonedDateTime actionStartTime;
    private ZonedDateTime actionTimeOut;
    private PmsWorkflow pending;
    private PmsWorkflow completed;
    private PmsWorkflow showFlow;
    private LevelDetails levelDetails;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
