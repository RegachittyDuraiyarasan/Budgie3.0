package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminLeaveCalenderDTO {
    private String empId;
    private String reviewerId;
    private String reportingManagerId;
    private String department;
    private String designation;
    private String payRollStatus;
    private String band;
    private String location;
    private LocalDate fromDate;
    private LocalDate toDate;
    @NotBlank(message = "yearMonth is mandatory")
    private String yearMonth;

}
