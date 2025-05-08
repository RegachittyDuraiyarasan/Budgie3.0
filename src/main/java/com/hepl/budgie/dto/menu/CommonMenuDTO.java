package com.hepl.budgie.dto.menu;

import java.util.List;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Condition;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonMenuDTO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String menuId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String subMenuId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @NotBlank(message = "{validation.error.menu.nameCannotBeBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @NotBlank(message = "{validation.error.menu.pathCannotBeBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    private String path;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @NotBlank(message = "{validation.error.menu.iconCannotBeBlank}", groups = { CreateDTO.class })
    private String icon;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String status;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @ValueOfEnum(enumClass = YesOrNoEnum.class, args = "Yes, No")
    private String condition;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private List<@Valid Condition> conditionList;

}
