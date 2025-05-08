package com.hepl.budgie.config.security;

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Component;

import com.hepl.budgie.dto.role.AuthorizationObj;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.service.role.RoleService;

import lombok.RequiredArgsConstructor;

@Component("authz")
@RequiredArgsConstructor
public class CustomAuthorization {

    private final JWTHelper jwtHelper;
    private final RoleService roleService;

    public boolean decide(MethodSecurityExpressionOperations operations, String menu, String submenu,
            String permission) {
        UserRef userRef = jwtHelper.getUserRefDetail();

        return roleService.checkMenuIsAccessible(AuthorizationObj.builder().groupId(userRef.getOrganizationGroupCode())
                .menu(menu).submenu(submenu).permission(permission).role(userRef.getActiveRole())
                .groupId(userRef.getOrganizationGroupCode()).build());
    }

}
