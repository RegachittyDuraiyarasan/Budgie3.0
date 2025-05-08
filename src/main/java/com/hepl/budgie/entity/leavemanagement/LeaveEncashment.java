package com.hepl.budgie.entity.leavemanagement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Document(collection = "leave_encashment")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LeaveEncashment {
    @Id
    private String id;
    private String empId;
    private String empName;
    private String year;
    @NotBlank(message = "Leave Type is required")
    private String leaveType;
    @NotNull(message = "Num of Days is required")
    @DecimalMin(value = "1.0", inclusive = false, message = "Number of days must be greater than 0")
    private double noOfDays;
    private double balance;
    private String status= "Pending";
    private String remarks;
    // @CreatedBy
    // private String createdBy;
    // @LastModifiedBy
    // private String updatedBy;
    // @CreatedDate
    // private LocalDateTime createdAt;
    // @LastModifiedDate
    // private LocalDateTime updatedAt;
}
