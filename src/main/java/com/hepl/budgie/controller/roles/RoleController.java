package com.hepl.budgie.controller.roles;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.menu.MenuStatus;
import com.hepl.budgie.dto.role.RoleBaseFieldsDTO;
import com.hepl.budgie.dto.role.RoleDTO;
import com.hepl.budgie.entity.role.Roles;
import com.hepl.budgie.service.role.RoleService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Manage roles", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
@Slf4j
public class RoleController {

    private final RoleService rolesService;
    private final Translator translator;

    @PostMapping()
    public GenericResponse<String> addRole(@Valid @RequestBody RoleDTO role) {
        log.info("Add role");
        rolesService.saveRole(role);

        return GenericResponse.success(translator.toLocale(AppMessages.ROLE_ADDED));
    }

    @PutMapping("/menu/{id}/status")
    public GenericResponse<String> updateMenuStatus(@PathVariable String id,
            @Valid @RequestBody MenuStatus menuStatus) {
        log.info("Update role status for menu - {}", id);

        rolesService.updateMenuStatus(id, menuStatus);
        return GenericResponse.success(translator.toLocale(AppMessages.ROLE_MENU_STATUS));
    }

    @PutMapping("/submenu/{id}/status/{menu}")
    public GenericResponse<String> updateSubmenuStatus(@PathVariable String id, @PathVariable String menu,
            @RequestBody MenuStatus status) {
        log.info("Update role status for submenu - {}", id);

        rolesService.updateSubmenuStatus(id, menu, status);
        return GenericResponse.success(translator.toLocale(AppMessages.ROLE_SUBMENU_STATUS));
    }

    @PutMapping("/{id}")
    public GenericResponse<String> editRole(@PathVariable String id, @Valid @RequestBody RoleDTO role) {
        log.info("Edit role - {}", id);

        rolesService.updateRole(role, id);
        return GenericResponse.success(translator.toLocale(AppMessages.ROLE_UPDATED));
    }

    @GetMapping("/{id}")
    public GenericResponse<Roles> getRole(@PathVariable String id) {
        log.info("Get role");

        return GenericResponse.success(rolesService.getRoleById(id));
    }

    @GetMapping("/all")
    public GenericResponse<List<Roles>> allRoles() {
        log.info("Fetch roles");
        return GenericResponse.success(rolesService.allRoleAndItsMenu());
    }

    @GetMapping()
    public GenericResponse<List<RoleBaseFieldsDTO>> fetchAllRoles() {
        log.info("Fetching all roles");

        return GenericResponse.success(rolesService.allRoles());
    }

    @GetMapping("/options")
    public GenericResponse<List<OptionsResponseDTO>> getRoleOptions(
            @RequestParam(defaultValue = "", required = false) String orgCode) {
        log.info("Get roles based on orgCode {}", orgCode);

        return GenericResponse
                .success(orgCode.isEmpty() ? Collections.emptyList() : rolesService.fetchOptions(orgCode));
    }

}
