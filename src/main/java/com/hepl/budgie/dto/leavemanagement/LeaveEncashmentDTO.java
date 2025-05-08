package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LeaveEncashmentDTO {
    private String empId;
    private String year;
    @NotNull(message = "Status cannot be null")
    private String status;
    private String remarks;
    @AssertTrue(message = "Remark is required")
    private boolean isRemarks() {
        if (status.equals("Reject")) {
            return remarks != null && !remarks.isEmpty(); // If either date is null, it's considered valid
        }
        return true;
    }
}
