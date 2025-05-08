package com.hepl.budgie.repository.menu;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.hepl.budgie.dto.menu.MenuAndSubmenuDTO;
import com.hepl.budgie.dto.menu.UpdateSubMenuDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.dto.menu.MenuDTO;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Menu;
import com.hepl.budgie.entity.menu.SubMenu;
import com.mongodb.client.result.UpdateResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public interface MenuRepository extends MongoRepository<Menu, String> {

    public static final String COLLECTION_NAME = "menus";

    default void updateMenu(String menuId, MenuDTO menuDTO, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("menuId").is(menuId));

        Update update = new Update();
        update.set("name", menuDTO.getName());
        update.set("path", menuDTO.getPath());
        update.set("icon", menuDTO.getIcon());
        update.set("condition", menuDTO.getCondition());
        update.set("conditionList", menuDTO.getConditionList());
        update.set("isSubmenu", menuDTO.getIsChild().equals(YesOrNoEnum.YES.label));
        update.set("order", menuDTO.getOrder());

        mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
    }

    default void updateMenuStatus(String menuId, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("menuId").is(menuId));

        Menu menu = mongoTemplate.findOne(query, Menu.class, COLLECTION_NAME);

        if (menu == null || Status.DELETED.label.equalsIgnoreCase(menu.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.MENU_ID);
        }

        String currentStatus = menu.getStatus();
        String newStatus = Status.ACTIVE.label.equalsIgnoreCase(currentStatus)
                ? Status.INACTIVE.label
                : Status.ACTIVE.label;

        Update update = new Update().set("status", newStatus);
        mongoTemplate.updateFirst(query, update, Menu.class, COLLECTION_NAME);
    }

    default UpdateResult addSubmenuByMenuId(String id, SubMenu submenu, MongoTemplate mongoTemplate) {

        Query query = new Query(
                new Criteria().andOperator(Criteria.where("_id").is(id), Criteria.where("isSubmenu").is(true)));

        Update update = new Update();
        update.push("submenus", submenu);

        return mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
    }

    default List<MenuDTO> getAllMenuAndItsSubMenu(MongoTemplate mongoTemplate) {
        MatchOperation matchMenus = Aggregation.match(Criteria.where("status").ne("Deleted"));
        UnwindOperation unwindOperation = Aggregation.unwind("submenus", true);
        ComparisonOperators.Eq childEqual = ComparisonOperators.Eq.valueOf("isSubmenu").equalToValue(true);
        ConditionalOperators.Cond isChildCond = ConditionalOperators.when(childEqual).then("Yes").otherwise("No");
        ProjectionOperation projectionOperation = Aggregation.project("menuId","name").and("submenus").as("submenu")
                .and(isChildCond).as("isChild");


        Aggregation aggregation = Aggregation.newAggregation(matchMenus,unwindOperation, projectionOperation);
        return mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                MenuDTO.class).getMappedResults();
    }

    List<Menu> findByNameIn(List<String> menuNames);

    Menu findTopByOrderByMenuIdDesc();

    default UpdateResult deleteMenuOptions(String menuId, MongoTemplate mongoTemplate) {

        Query query = new Query(Criteria.where("menuId").is(menuId));

        Update update = new Update().set("status", Status.DELETED.label);

        return mongoTemplate.updateFirst(query, update, Menu.class, COLLECTION_NAME);
    }

    default UpdateResult deleteSubmenuOptions(String subMenuId, MongoTemplate mongoTemplate) {

        Query query = new Query(Criteria.where("submenus.subMenuId").is(subMenuId));

        Update update = new Update().set("submenus.$.status", Status.DELETED.label);

        return mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
    }

    default void updateSubMenuStatus(String subMenuId, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("submenus.subMenuId").is(subMenuId));

        Menu menu = mongoTemplate.findOne(query, Menu.class, COLLECTION_NAME);

        SubMenu targetSubMenu = menu.getSubmenus().stream()
                .filter(sm -> subMenuId.equals(sm.getSubMenuId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.SUB_MENU_ID));

        if (Status.DELETED.label.equalsIgnoreCase(targetSubMenu.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.SUB_MENU_ID);
        }

        String currentStatus = targetSubMenu.getStatus();
        String newStatus = Status.ACTIVE.label.equalsIgnoreCase(currentStatus)
                ? Status.INACTIVE.label
                : Status.ACTIVE.label;

        Update update = new Update().set("submenus.$.status", newStatus);
        mongoTemplate.updateFirst(query, update, Menu.class, COLLECTION_NAME);
    }

    default void updateSubMenu(String subMenuId, UpdateSubMenuDTO updateSubMenuDTO, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("submenus.subMenuId").is(subMenuId));

        Update update = new Update();
        update.set("submenus.$.name", updateSubMenuDTO.getName());
        update.set("submenus.$.path", updateSubMenuDTO.getPath());
        update.set("submenus.$.order", updateSubMenuDTO.getOrder());
        update.set("submenus.$.condition", updateSubMenuDTO.getCondition());
        update.set("submenus.$.conditionList", updateSubMenuDTO.getConditionList());

        mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
    }

    default List<MenuAndSubmenuDTO> getMenuAndSubmenu(MongoTemplate mongoTemplate, String menuId) {
        // If menuId is not passed, fetch only menuId and name
        if (menuId == null || menuId.isEmpty()) {
            Query query = new Query();
            query.fields().include("menuId").include("name");

            List<Menu> menus = mongoTemplate.find(query, Menu.class, "menus");

            return menus.stream()
                    .map(menu -> {
                        MenuAndSubmenuDTO dto = new MenuAndSubmenuDTO();
                        dto.setMenuId(menu.getMenuId());
                        dto.setName(menu.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else {
            // If menuId is passed, fetch only that menu's submenus
            Query query = new Query(Criteria.where("menuId").is(menuId));
            query.fields().include("submenus");

            Menu menu = mongoTemplate.findOne(query, Menu.class, "menus");

            if (menu == null || menu.getSubmenus() == null) {
                return Collections.emptyList();
            }

            return menu.getSubmenus().stream()
                    .map(submenu -> {
                        MenuAndSubmenuDTO dto = new MenuAndSubmenuDTO();
                        dto.setMenuId(menu.getMenuId());
                        dto.setSubMenuId(submenu.getSubMenuId());
                        dto.setName(submenu.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
    }

}
