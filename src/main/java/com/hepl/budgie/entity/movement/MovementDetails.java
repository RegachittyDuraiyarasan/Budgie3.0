package com.hepl.budgie.entity.movement;

import com.hepl.budgie.config.auditing.AuditInfo;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
public class MovementDetails extends AuditInfo {
    private String movementId;
    private String movementInitializer;
    private Integer movementTypeId;
    private Integer movementTeamId;
    private MovementType movementType;
    private MovementTransferDetails department;
    private MovementTransferDetails designation;
    private MovementTransferDetails supervisor;
    private MovementTransferDetails reviewer;
    private String supervisorRemarks;
    private ZonedDateTime effectiveFrom;
    private ApprovalStatus assignedReviewerStatus;
    private ApprovalStatus officialReviewerStatus;
    private HRStatus hrStatus;
    private Boolean isWithdrawn;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    @CreatedDate
    private ZonedDateTime createdAt;
    @LastModifiedDate
    private ZonedDateTime updatedAt;

}
