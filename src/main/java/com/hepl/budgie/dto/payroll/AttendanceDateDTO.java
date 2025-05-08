package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.entity.Status;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttendanceDateDTO {
    @NotBlank(message = "{validation.error.notBlank}")
    private String standardStartDate;
    @NotBlank(message = "{validation.error.notBlank}")
    private String standardEndDate;
    private String finYear;
    private String fromFinYear;
    private String toFinYear;
    private String orgId;
    private String status = Status.ACTIVE.label;
    @AssertTrue(message = "Start date must be less than end date")
    public boolean isStandardEndDate() {
        return Integer.parseInt(standardStartDate) > Integer.parseInt(standardEndDate);
    }
}
