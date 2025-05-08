package com.hepl.budgie.service.impl.role;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.menu.MenuStatus;
import com.hepl.budgie.dto.role.AuthorizationObj;
import com.hepl.budgie.dto.role.MenuReqDTO;
import com.hepl.budgie.dto.role.RoleBaseFieldsDTO;
import com.hepl.budgie.dto.role.RoleDTO;
import com.hepl.budgie.dto.role.SubmenuReqDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.menu.CommonMenuFields;
import com.hepl.budgie.entity.menu.Menu;
import com.hepl.budgie.entity.menu.SubMenu;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.role.Permissions;
import com.hepl.budgie.entity.role.Roles;
import com.hepl.budgie.entity.role.SubmenuPermission;
import com.hepl.budgie.repository.master.RolesRepository;
import com.hepl.budgie.repository.menu.MenuRepository;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.service.role.RoleService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RolesRepository rolesRepository;
    private final MenuRepository menuRepository;
    private final OrganizationRepository organizationRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<Roles> allRoleAndItsMenu() {
        log.info("Fetch roles");
        return rolesRepository.fetchAllRolesAndMenus(jwtHelper.getOrganizationGroupCode(), mongoTemplate);
    }

    @Override
    public Roles getRoleById(String id) {
        log.info("Fetch role");
        return rolesRepository.fetchByRoleIdAndGrpCode(id, jwtHelper.getOrganizationGroupCode(), mongoTemplate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
    }

    @Override
    public List<RoleBaseFieldsDTO> allRoles() {
        log.info("Fetching all roles");
        return rolesRepository.fetchAllRoles(jwtHelper.getOrganizationGroupCode(), mongoTemplate).getMappedResults();
    }

    @Override
    public void initIndexingRoleForOrganisation(String groupCode) {
        log.info("Init master roles");
        rolesRepository.initMasterRoles(groupCode,mongoTemplate);
    }

    @Override
    public void updateRole(RoleDTO roleDTO, String id) {
        log.info("Updating roles");

        Roles role = generateRolesFromDTO(roleDTO);
        role.setId(id);

        rolesRepository.updateRole(role, id, jwtHelper.getOrganizationGroupCode(), mongoTemplate,
                jwtHelper.getUserRefDetail());
    }

    @Override
    public List<OptionsResponseDTO> fetchOptions(String org) {
        log.info("Fetch roles");
        String[] orgList = org.split(",");
        Organization organization = organizationRepository.findByOrganizationCode(orgList[0]).orElse(null);
        if (organization != null) {
            List<OptionsResponseDTO> optionList = rolesRepository.getOptions(mongoTemplate, organization.getGroupId())
                    .getMappedResults();
            return optionList.isEmpty() ? Collections.emptyList() : optionList;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean checkMenuIsAccessible(AuthorizationObj authorizationObj) {
        log.info("Check id menu accessible");
        return rolesRepository.checkRoleAccess(authorizationObj, mongoTemplate) > 0;
    }

    @Override
    public void saveRole(RoleDTO roleDTO) {
        log.info("Save role");
        Roles role = generateRolesFromDTO(roleDTO);

        rolesRepository.saveRole(role, jwtHelper.getOrganizationGroupCode(), mongoTemplate);

    }

    private Roles generateRolesFromDTO(RoleDTO roleDTO) {
        List<String> menuNames = roleDTO.getPermissions().stream().map(MenuReqDTO::getMenuName).toList();
        List<Menu> menus = menuRepository.findByNameIn(menuNames);

        Map<String, Menu> menuMap = new HashMap<>();
        for (Menu menu : menus) {

            Map<String, SubMenu> tmpSubMenuMap = new HashMap<>();
            for (SubMenu subMenu : menu.getSubmenus()) {
                tmpSubMenuMap.put(subMenu.getName(), subMenu);
            }
            menu.setSubmenuMap(tmpSubMenuMap);
            menuMap.put(menu.getName(), menu);
        }

        Roles role = new Roles();
        role.setRoleName(roleDTO.getRoleName());
        role.setRoleDescription(roleDTO.getRoleDescription());
        role.setPermissions(buildRole(menuMap, roleDTO));
        role.setStatus(Status.ACTIVE.label);

        return role;
    }

    private List<Permissions> buildRole(Map<String, Menu> menuMap, RoleDTO roleDTO) {
        log.info("Build roles");
        List<Permissions> permissions = new ArrayList<>();

        for (MenuReqDTO menuReq : roleDTO.getPermissions()) {
            Menu tmpMenu = menuMap.get(menuReq.getMenuName());
            CommonMenuFields menu = CommonMenuFields.builder().name(tmpMenu.getName()).icon(tmpMenu.getIcon())
                    .path(tmpMenu.getPath()).condition(tmpMenu.getCondition()).conditionList(tmpMenu.getConditionList())
                    .status(tmpMenu.getStatus())
                    .permissions(menuReq.getPermission())
                    .hasSubmenu(tmpMenu.isSubmenu())
                    .build();
            List<SubmenuPermission> subMenuPermissions = new ArrayList<>();
            for (SubmenuReqDTO submenuReq : menuReq.getSubmenu()) {
                SubMenu tmpSubMenu = menuMap.get(menuReq.getMenuName()).getSubmenuMap()
                        .get(submenuReq.getSubmenuName());
                CommonMenuFields submenu = CommonMenuFields.builder().name(tmpSubMenu.getName())
                        .icon(tmpSubMenu.getIcon())
                        .path(tmpSubMenu.getPath()).condition(tmpSubMenu.getCondition())
                        .status(tmpSubMenu.getStatus())
                        .conditionList(tmpSubMenu.getConditionList()).build();

                subMenuPermissions.add(new SubmenuPermission(submenu, submenuReq.getPermission()));
            }

            permissions.add(new Permissions(menu, subMenuPermissions));
        }
        return permissions;
    }

    @Override
    public void updateMenuStatus(String id, MenuStatus menuStatus) {
        log.info("Updating menu status for role");
        rolesRepository.updateMenuStatus(mongoTemplate, menuStatus, id, jwtHelper.getOrganizationGroupCode());
    }

    @Override
    public void updateSubmenuStatus(String id, String menu, MenuStatus menuStatus) {
        log.info("Updating submenu status for role");
        rolesRepository.updateSubMenuStatus(mongoTemplate, menuStatus, id, menu, jwtHelper.getOrganizationGroupCode());
    }

}
