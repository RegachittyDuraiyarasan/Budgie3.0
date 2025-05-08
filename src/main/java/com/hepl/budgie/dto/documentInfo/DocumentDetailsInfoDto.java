package com.hepl.budgie.dto.documentInfo;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.config.annotation.FileChecker;
import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.entity.master.AcknowledgementTypeEnum;
import com.hepl.budgie.entity.master.DependencyCondType;

import com.hepl.budgie.entity.master.DocumentTypeEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentDetailsInfoDto {

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @ValueOfEnum(enumClass = DocumentTypeEnum.class, message = "{validation.error.invalid}")
    private String moduleId;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @ValueOfEnum(enumClass = DocumentTypeEnum.class, message = "{validation.error.invalid}")
    private String documentCategory;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String title;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String description;
    @NotBlank(message = "{validation.error.notBlank}")

    // private FileDetailsDto fileDetailsDto;
    
    private MultipartFile fileUpload;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String acknowledgedType;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String acknowledgementHeading;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String acknowledgementDescription;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private String status;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private ZonedDateTime createdDate;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private ZonedDateTime lastModifiedfDate;

}
