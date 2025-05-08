package com.hepl.budgie.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
public class OrganizationMapAddDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String groupId;

    @NotBlank(message = "{validation.error.notBlank}")
    private String organizationDetail;

    @NotEmpty(message = "{validation.error.notBlank}")
    private List<String> organizationMapping;

}
