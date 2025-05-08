package com.hepl.budgie.service.role;

import java.util.List;

import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.menu.MenuStatus;
import com.hepl.budgie.dto.role.AuthorizationObj;
import com.hepl.budgie.dto.role.RoleBaseFieldsDTO;
import com.hepl.budgie.dto.role.RoleDTO;
import com.hepl.budgie.entity.role.Roles;

public interface RoleService {

    void initIndexingRoleForOrganisation(String groupcode);

    List<OptionsResponseDTO> fetchOptions(String org);

    void saveRole(RoleDTO role);

    boolean checkMenuIsAccessible(AuthorizationObj authorizationObj);

    Roles getRoleById(String id);

    List<Roles> allRoleAndItsMenu();

    List<RoleBaseFieldsDTO> allRoles();

    void updateRole(RoleDTO roleDTO, String id);

    void updateMenuStatus(String id, MenuStatus menuStatus);

    void updateSubmenuStatus(String id, String menu, MenuStatus menuStatus);

}
