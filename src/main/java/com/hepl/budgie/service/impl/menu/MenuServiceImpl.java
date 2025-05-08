package com.hepl.budgie.service.impl.menu;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hepl.budgie.dto.menu.*;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.utils.AppUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Menu;
import com.hepl.budgie.entity.menu.SubMenu;
import com.hepl.budgie.mapper.menu.MenuMapper;
import com.hepl.budgie.repository.menu.MenuRepository;
import com.hepl.budgie.service.menu.MenuService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MongoTemplate mongoTemplate;
    private final MenuMapper menuMapper;
    private static final String MENU_ID_SEQUENCE = "ME000";
    private static final String SUBMENU_ID_SEQUENCE = "SB000";

    public MenuServiceImpl(MenuRepository menuRepository, MongoTemplate mongoTemplate, MenuMapper menuMapper) {
        this.menuRepository = menuRepository;
        this.mongoTemplate = mongoTemplate;
        this.menuMapper = menuMapper;
    }

    @Override
    public void saveMenu(MenuDTO menuDTO) {
        log.info("Saving menu");
        String lastMenuId = Optional.ofNullable(menuRepository.findTopByOrderByMenuIdDesc())
                .map(Menu::getMenuId)
                .orElse(MENU_ID_SEQUENCE);
        String newMenuId = AppUtils.generateUniqueId(lastMenuId);
        log.info("Generated Menu ID: {}", newMenuId);
        Menu menu = new Menu();
        menu.setMenuId(newMenuId);
        menu.setName(menuDTO.getName());
        menu.setPath(menuDTO.getPath());
        menu.setIcon(menuDTO.getIcon());
        menu.setOrder(menuDTO.getOrder());
        menu.setCondition(menuDTO.getCondition());
        menu.setConditionList(menuDTO.getConditionList());
        menu.setStatus(Status.ACTIVE.label);
        menu.setSubmenu(YesOrNoEnum.YES.label.equals(menuDTO.getIsChild()));

        menuRepository.save(menu);
    }

    @Override
    public void updateMenu(MenuDTO menuDTO, String menuId) {
        log.info("Update menu by {}", menuId);

        menuRepository.updateMenu(menuId, menuDTO, mongoTemplate);
    }

    @Override
    public List<MenuDTO> allMenus() {
        log.info("Fetch all menus");
        return menuRepository.findAll().stream()
                .filter(menu -> !Status.DELETED.label.equalsIgnoreCase(menu.getStatus()))
                .map(menuMapper::toMenuDTO)
                .toList();
    }

    @Override
    public void updateMenuStatus(String menuId) {
        log.info("Update menu status by {}", menuId);
        menuRepository.updateMenuStatus(menuId, mongoTemplate);
    }

    @Override
    public void deleteMenu(String menuId){
        UpdateResult result = menuRepository.deleteMenuOptions(menuId, mongoTemplate);
        log.info("result {}",result);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void saveSubmenu(CommonMenuDTO submenuDTO, String id) {
        log.info("Saving submenu");
        List<Menu> allMenus = menuRepository.findAll();

        String lastSubMenuId = allMenus.stream()
                .flatMap(menu -> menu.getSubmenus() != null ? menu.getSubmenus().stream() : Stream.empty())
                .map(SubMenu::getSubMenuId)
                .filter(Objects::nonNull)
                .max(String::compareTo)
                .orElse(SUBMENU_ID_SEQUENCE);

        String newSubMenuId = AppUtils.generateUniqueId(lastSubMenuId);

        SubMenu subMenu = new SubMenu();
        subMenu.setSubMenuId(newSubMenuId);
        subMenu.setName(submenuDTO.getName());
        subMenu.setPath(submenuDTO.getPath());
        subMenu.setIcon(submenuDTO.getIcon());
        subMenu.setCondition(submenuDTO.getCondition());
        subMenu.setConditionList(submenuDTO.getConditionList());
        subMenu.setStatus(Status.ACTIVE.label);

        UpdateResult result = menuRepository.addSubmenuByMenuId(id, subMenu, mongoTemplate);
        if (result.getModifiedCount() == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.SUB_MENU_CANNOT_ADD);

    }

    @Override
    public void updateSubMenu(UpdateSubMenuDTO updateSubMenuDTO, String subMenuId) {
        log.info("Update sub menu by {}", subMenuId);

        menuRepository.updateSubMenu(subMenuId, updateSubMenuDTO, mongoTemplate);
    }

    @Override
    public List<MenuDTO> allSubmenus() {
        log.info("Fetch all sub menus");
        List<MenuDTO> filteredList = menuRepository.getAllMenuAndItsSubMenu(mongoTemplate).stream()
                .peek(menu -> {
                    if (menu.getSubmenu() != null && Status.DELETED.label.equalsIgnoreCase(menu.getSubmenu().getStatus())) {
                        menu.setSubmenu(null);
                    }
                })
                .collect(Collectors.toList());

        log.info("ALL menus-{}", filteredList);
        return filteredList;
    }

    @Override
    public void updateSubMenuStatus(String subMenuId) {
        log.info("Update sub menu status by {}", subMenuId);
        menuRepository.updateSubMenuStatus(subMenuId, mongoTemplate);
    }

    @Override
    public void deleteSubMenu(String subMenuId){
        UpdateResult result = menuRepository.deleteSubmenuOptions(subMenuId, mongoTemplate);
        log.info("deleteSubMenu {}",result);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public List<MenuAndSubmenuDTO> getMenuAndSubmenu(String menuId){

        return menuRepository.getMenuAndSubmenu(mongoTemplate,menuId);
    }

}
