package com.hepl.budgie.dto.organization;

import com.hepl.budgie.config.annotation.FileChecker;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.entity.organization.Sequence;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationAddDTO {

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String organizationDetail;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String groupId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String organizationCode;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Invalid Email", groups = { CreateDTO.class,
            UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String email;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String industryType;


    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String tdsCircle;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String logo;

    @FileChecker(ext = "image/jpeg,image/png,image/svg+xml", isMandatory = true, message = "{validation.error.fileFormatNotSupported}", allowedFormatArgs = ".jpeg, .png", groups = {
            CreateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = AccessMode.WRITE_ONLY)
    private MultipartFile logoFile;

    @FileChecker(ext = "image/jpeg,image/png,image", isMandatory = true, message = "{validation.error.fileFormatNotSupported}", allowedFormatArgs = ".jpeg, .png", groups = {
            CreateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private MultipartFile headSignature;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String signature;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String contactNumber;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String address;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String gstNumber;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String country;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String state;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String town;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String pinOrZipcode;

    @NotEmpty(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private List<Sequence> sequence;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String smtpProvider;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String smtpServer;

    @NotNull(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private long smtpPort;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", groups = { CreateDTO.class,
            UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String fromMail;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String userName;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String password;

}
