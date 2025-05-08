package com.hepl.budgie.dto;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hepl.budgie.config.annotation.FileChecker;
import com.hepl.budgie.config.jackson.ForceStringDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = Include.NON_NULL)
public class UserDTO {

    @Schema(accessMode = AccessMode.READ_ONLY)
    private String id;

    @NotBlank(message = "{validation.user.userIdCannotBeEmpty}")
    @Size(max = 6, message = "{validation.user.userIdMustBeMaximumOf_6Characters}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String userId;

    @NotBlank(message = "{validation.user.usernameCannotBeEmpty}")
    @Size(max = 20, message = "{validation.user.usernameMustBeMaximumOf_20Characters}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String username;

    @NotBlank(message = "{validation.user.emailCannotBeEmpty}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "test@hepl.com", description = "Encrypt data using AES/GCM")
    @Pattern(regexp = "^[a-z0-9]{1,25}(?:[._+][a-z0-9]+)*@[a-z0-9.-]+\\.[a-z]{2,}$", message = "{validation.user.emailIsInvalid}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String email;

    @NotBlank(message = "{validation.user.mobileCannotBeEmpty}")
    @Pattern(regexp = "^[+]*[(]?\\d{1,4}[)]?[-\\s\\./0-9]{10,15}$", message = "{validation.user.mobileNoIsInvalid}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Encrypt data using AES/GCM", example = "+128484444545")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String mobile;

    @NotBlank(message = "{validation.user.roleIdCannotBeEmpty}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String roleId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.WRITE_ONLY)
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String password;

    @Schema(accessMode = AccessMode.WRITE_ONLY)
    @FileChecker(ext = "image/png, image/jpg, image/jpeg", isMandatory = false, message = "{error.fileNotSupported}", allowedFormatArgs = ".jpg, .jpeg")
    private MultipartFile profilePhoto;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private String profilePhotoPath;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private String status;

}
