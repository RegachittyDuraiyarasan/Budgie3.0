package com.hepl.budgie.mapper.menu;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.hepl.budgie.dto.menu.MenuDTO;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Menu;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "isChild", source = "submenu", qualifiedByName = "setMenuChild")
    MenuDTO toMenuDTO(Menu menu);

    @Named("setMenuChild")
    default String setMenuChild(boolean isSubmenu) {
        return isSubmenu ? YesOrNoEnum.YES.label : YesOrNoEnum.NO.label;
    }

}
