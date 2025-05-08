package com.hepl.budgie.dto.probation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ProbationFetchDTO {
    private String empName;
    private String reportingManagerName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private ZonedDateTime probationStartDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private ZonedDateTime probationEndDate;
    private String results;
    private String extendedMonths;
    private String status;
    private String extendedStatus;
    private String hrVerifyStatus;
}
