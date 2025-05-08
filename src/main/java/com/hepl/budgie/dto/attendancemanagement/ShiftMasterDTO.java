package com.hepl.budgie.dto.attendancemanagement;

import java.util.List;

import com.hepl.budgie.entity.attendancemanagement.BreakTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShiftMasterDTO {

    @NotBlank(message = "Shift code is required")
    private String shiftCode;

    @NotBlank(message = "Shift name is required")
    private String shiftName;

    @NotBlank(message = "In time is required")
    private String inTime;

    @NotBlank(message = "Out time is required")
    private String outTime;

    @NotBlank(message = "Half-day time is required")
    private String halfDayTime;

    @NotNull(message = "Break times cannot be null")
    private List<BreakTime> breakTime;
}