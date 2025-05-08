package com.hepl.budgie.dto.menu;

import java.util.List;

import com.hepl.budgie.entity.menu.Condition;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SubMenuDTO extends CommonMenuDTO {

    @Builder
    public SubMenuDTO(String menuId, String subMenuId, String name, String path, String icon, String status, String condition,
            List<Condition> conditionList) {
        super(menuId, subMenuId, name, path, icon, status, condition, conditionList);
    }

}
