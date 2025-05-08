package com.hepl.budgie.dto.movement;

import com.hepl.budgie.entity.movement.MovementType;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
public class MovementInitiateDTO {
    private String empId;
    private MovementType movementType;
    private String movementInitializer;
    private Integer movementTypeId;
    private Integer movementTeamId;
    private String departmentOld;
    private String departmentNow;
    private String designationOld;
    private String designationNow;
    private String supervisorOld;
    private String supervisorNow;
    private String reviewerOld;
    private String reviewerNow;
    private String supervisorRemarks;
    private ZonedDateTime effectiveFrom;
    private String hrStatus;
    private String officialReviewerStatus;
    private String assignedReviewerStatus;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;


}
