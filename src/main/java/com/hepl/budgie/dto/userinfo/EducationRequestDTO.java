package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.config.annotation.FileChecker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data

public class EducationRequestDTO {
    private List<@Valid EduDetails> eduDetails;
    @Data
    public static class EduDetails {
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
        @FileChecker(
                ext = "image/png, image/jpg, image/jpeg, application/pdf",
                isMandatory = true,
                message = "{error.fileNotSupported}",
                allowedFormatArgs = ".png, .jpg, .jpeg, .pdf"
        )
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
}
