package com.hepl.budgie.controller.menu;

import com.hepl.budgie.dto.menu.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.service.menu.MenuService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = " Menu details", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final Translator translator;

    @PostMapping()
    public GenericResponse<String> addMenuAndSubmenu(@Validated(CreateDTO.class) @RequestBody MenuDTO menu) {
        log.info("Add menu and submenu");
        menuService.saveMenu(menu);

        return GenericResponse.success(translator.toLocale(AppMessages.MENU_ADDED_SUCCESS));
    }

    @PutMapping("/{menuId}")
    public GenericResponse<String> editMenu(@PathVariable String menuId,
                                            @Validated(CreateDTO.class) @RequestBody MenuDTO menu) {
        log.info("Update menu {}", menuId);
        menuService.updateMenu(menu, menuId);

        return GenericResponse.success(translator.toLocale(AppMessages.MENU_UPDATED_SUCCESS));
    }

    @GetMapping()
    public GenericResponse<List<MenuDTO>> getAllMenus() {
        log.info("All menus");
        return GenericResponse.success(menuService.allMenus());
    }

    @PutMapping("/status/{menuId}")
    public GenericResponse<String> updateMenuStatus(@PathVariable String menuId) {
        menuService.updateMenuStatus(menuId);
        return GenericResponse.success(translator.toLocale(AppMessages.MENU_STATUS));
    }

    @DeleteMapping("/{menuId}")
    public GenericResponse<String> deleteMenu(@PathVariable String menuId){
        menuService.deleteMenu(menuId);
        return GenericResponse.success(translator.toLocale(AppMessages.MENU_DELETED));
    }

    @PutMapping("/submenu/{menuId}")
    public GenericResponse<String> updateSubmenu(@PathVariable String menuId,
            @Validated(UpdateDTO.class) @RequestBody CommonMenuDTO subMenuDTO) {
        log.info("Add submenu");
        menuService.saveSubmenu(subMenuDTO, menuId);

        return GenericResponse.success(translator.toLocale(AppMessages.SUBMENU_ADDED_SUCCESS));
    }

    @PutMapping("/subMenu/{subMenuId}")
    public GenericResponse<String> editSubMenu(@PathVariable String subMenuId,
                                               @Validated(CreateDTO.class) @RequestBody UpdateSubMenuDTO updateSubMenuDTO) {
        log.info("Update sub menu {}", subMenuId);
        menuService.updateSubMenu(updateSubMenuDTO, subMenuId);

        return GenericResponse.success(translator.toLocale(AppMessages.SUB_MENU_UPDATED_SUCCESS));
    }

    @GetMapping("/submenus")
    public GenericResponse<List<MenuDTO>> getAllSubMenus() {
        log.info("All sub menus");
        return GenericResponse.success(menuService.allSubmenus());
    }

    @PutMapping("/subMenu-status/{subMenuId}")
    public GenericResponse<String> updateSubMenuStatus(@PathVariable String subMenuId) {
        menuService.updateSubMenuStatus(subMenuId);
        return GenericResponse.success(translator.toLocale(AppMessages.SUB_MENU_STATUS));
    }

    @DeleteMapping("/subMenu/{subMenuId}")
    public GenericResponse<String> deleteSubMenu(@PathVariable String subMenuId){
        menuService.deleteSubMenu(subMenuId);
        return GenericResponse.success(translator.toLocale(AppMessages.SUB_MENU_DELETED));
    }

    @GetMapping("/menu-submenu")
    public GenericResponse<List<MenuAndSubmenuDTO>> getMenuAndSubmenu(@RequestParam(value = "menuId", required = false) String menuId){
        return GenericResponse.success(menuService.getMenuAndSubmenu(menuId));
    }


}
