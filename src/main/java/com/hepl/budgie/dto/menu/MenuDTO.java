package com.hepl.budgie.dto.menu;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Condition;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuDTO extends CommonMenuDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String id;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private int order;

    @Builder
    public MenuDTO(String menuId,String id, String subMenuId, String name, String path, String icon, String status, String isChild, String condition,
            List<Condition> conditionList) {
        super(menuId,subMenuId, name, path, icon, status, condition, conditionList);
        this.isChild = isChild;
        this.id = id;
    }

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    @ValueOfEnum(enumClass = YesOrNoEnum.class, message = "{validation.error.menu.valueMustBeEitherYesOrNo}")
    private String isChild;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private SubMenuDTO submenu;

}
