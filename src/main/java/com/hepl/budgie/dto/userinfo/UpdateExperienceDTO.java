package com.hepl.budgie.dto.userinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class UpdateExperienceDTO {
    @NotBlank(message = "ExperienceId is required")
    private String experienceId;
    @NotBlank(message = "Designation is required")
    private String jobTitle;
    @NotBlank(message = "Company Name is required")
    private String companyName;
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
    private MultipartFile files;
    @NotNull(message = "BeginOn is required")
    private LocalDate beginOn;
    private Boolean presentOn;
    private LocalDate endOn;
    private String status;

    @AssertTrue(message = "End Date must be greater than or equal to Start Date")
    private boolean isEndOnValid() {
        if (beginOn == null || endOn == null) {
            return true;
        }
        return !endOn.isBefore(beginOn);
    }

    @AssertTrue(message = "End Date must be provided when PresentOn is false")
    private boolean isEndOnRequired() {
        if (Boolean.TRUE.equals(presentOn)) return true;
        return endOn != null;
    }


    @AssertTrue(message = "Files must be provided when PresentOn is false")
    private boolean isFilesValid() {
        if(!presentOn){
            return files != null || !files.isEmpty();
        }
        return true;
    }

}
