package com.hepl.budgie.dto.iiy;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Data
public class CourseCategoryRequestDTO {

    @NotEmpty(message = "{validation.error.notBlank}")
   @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.WRITE_ONLY)
    private String categoryName;
    private String categoryId;
    private String id;
    private String status;

}
