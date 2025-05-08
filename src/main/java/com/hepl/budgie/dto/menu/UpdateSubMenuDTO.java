package com.hepl.budgie.dto.menu;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Condition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSubMenuDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @NotBlank(message = "{validation.error.menu.nameCannotBeBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    private String name;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @NotBlank(message = "{validation.error.menu.pathCannotBeBlank}", groups = { CreateDTO.class, UpdateDTO.class })
    private String path;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private int order;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @ValueOfEnum(enumClass = YesOrNoEnum.class, args = "Yes, No")
    private String condition;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private List<@Valid Condition> conditionList;

}
