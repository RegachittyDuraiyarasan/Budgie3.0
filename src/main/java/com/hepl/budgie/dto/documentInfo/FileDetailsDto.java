package com.hepl.budgie.dto.documentInfo;

import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileDetailsDto {
    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String folderName;

    @NotBlank(message = "{validation.error.notBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String fileName;
}
