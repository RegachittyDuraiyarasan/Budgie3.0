package com.hepl.budgie.dto.movement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hepl.budgie.entity.movement.MovementType;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class MovementFetchDTO {
    private String movementId;
    private String userName;
    private String empId;
    private String movementInitializer;
    private Integer movementTypeId;
    private Integer movementTeamId;
    private MovementType movementType;
    private String departmentOld;
    private String departmentNow;
    private String designationOld;
    private String designationNow;
    private String supervisorOld;
    private String supervisorNow;
    private String reviewerOld;
    private String reviewerNow;
    private String supervisorRemarks;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private ZonedDateTime effectiveFrom;
    private String assignedReviewerStatus;
    private String officialReviewerStatus;
    private String hrStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private ZonedDateTime createdAt;
    private Boolean isWithdrawn;

}
