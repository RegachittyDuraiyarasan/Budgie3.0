package com.hepl.budgie.dto.documentInfo;

import java.util.List;

import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.entity.documentinfo.DocumentDetailsInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DocumentInfoDto {
    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String empId;

    @NotBlank(message = "{validation.error.notBlank}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private List<DocumentDetailsInfoDto> docdetails;

}
