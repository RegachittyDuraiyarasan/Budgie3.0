package com.hepl.budgie.entity.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.hepl.budgie.entity.menu.CommonMenuFields;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permissions {

    private CommonMenuFields menu;
    private List<SubmenuPermission> subMenuPermissions;

}
