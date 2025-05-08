package com.hepl.budgie.dto.preonboarding;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeUpdateRequestDto {
    private String empId;
    @NotNull(message = "isSeatingRequestInitiated cannot be null")
    private Boolean isSeatingRequestInitiated;
    @NotNull(message = "isIdCardRequestInitiated cannot be null")
    private Boolean isIdCardRequestInitiated;
}
