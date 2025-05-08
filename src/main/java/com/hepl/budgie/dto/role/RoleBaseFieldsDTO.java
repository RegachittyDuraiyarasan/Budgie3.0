package com.hepl.budgie.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleBaseFieldsDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    private String roleName;
    private String roleDescription;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

}
