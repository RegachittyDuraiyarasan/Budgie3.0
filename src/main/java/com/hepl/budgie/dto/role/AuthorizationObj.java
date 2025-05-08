package com.hepl.budgie.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizationObj {

    private String role;
    private String menu;
    private String submenu;
    private String permission;
    private String groupId;

}
