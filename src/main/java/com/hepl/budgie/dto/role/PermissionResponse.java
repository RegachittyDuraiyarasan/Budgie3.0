package com.hepl.budgie.dto.role;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PermissionResponse extends CommonPermission {

    @Builder
    public PermissionResponse(String text, String path, String icon, List<SubmenuPermissionResponse> submenu) {
        super(text, path, icon);
        this.submenu = submenu;
    }

    private List<SubmenuPermissionResponse> submenu;
}
