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
public class SubmenuPermissionResponse extends CommonPermission {

    @Builder
    public SubmenuPermissionResponse(String text, String path, String icon, List<String> permission) {
        super(text, path, icon);
        this.permission = permission;
    }

    private List<String> permission;

}
