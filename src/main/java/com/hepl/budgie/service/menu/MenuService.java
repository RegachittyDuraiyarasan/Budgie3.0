package com.hepl.budgie.service.menu;

import java.util.List;

import com.hepl.budgie.dto.menu.*;

public interface MenuService {

    void saveMenu(MenuDTO menuDTO);

    void updateMenu(MenuDTO menuDTO, String menuId);

    void saveSubmenu(CommonMenuDTO submenu, String id);

    List<MenuDTO> allMenus();

    List<MenuDTO> allSubmenus();

    void updateMenuStatus(String menuId);

    void updateSubMenuStatus(String subMenuId);

    void deleteMenu(String menuId);

    void deleteSubMenu(String subMenuId);

    void updateSubMenu(UpdateSubMenuDTO updateSubMenuDTO, String subMenuId);

    List<MenuAndSubmenuDTO> getMenuAndSubmenu(String menuId);

}
