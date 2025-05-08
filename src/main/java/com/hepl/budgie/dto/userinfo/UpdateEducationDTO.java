package com.hepl.budgie.dto.userinfo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateEducationDTO {
    @NotBlank(message = "EducationId is required")
    private String educationId;
    private String qualificationId;
    @NotBlank(message = "Qualification is required")
    private String qualification;
    @NotBlank(message = "Course is required")
    private String course;
    @NotBlank(message = "Institute is required")
    private String institute;
    @NotNull(message = "BeginOn is required")
    private LocalDate beginOn;
    @NotNull(message = "EndOn is required")
    private LocalDate endOn;
    @NotBlank(message = "PercentageOrCgpa is required")
    private String percentageOrCgpa;
    private MultipartFile files;
    private String status;

    @AssertTrue(message = "End Date must be greater than or equal to Start Date")
    private boolean isEndOnValid() {
        if (beginOn == null || endOn == null) {
            return true; // If either date is null, it's considered valid
        }
        return !endOn.isBefore(beginOn);
    }

}
