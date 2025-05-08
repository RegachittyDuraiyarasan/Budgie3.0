package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.config.annotation.FileChecker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExperienceRequestDTO {

    private List<@Valid ExpDetail> expDetails;

    @Data
    public static class ExpDetail {
        @NotBlank(message = "Designation is required")
        private String jobTitle;
        @NotBlank(message = "Company Name is required")
        private String companyName;
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @FileChecker(
                ext = "image/png, image/jpg, image/jpeg, application/pdf",
                isMandatory = false,
                message = "{error.fileNotSupported}",
                allowedFormatArgs = ".png, .jpg, .jpeg, .pdf"
        )
        private MultipartFile files;
        @NotNull(message = "BeginOn is required")
        private LocalDate beginOn;
        private Boolean presentOn;
        @Field
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
}
