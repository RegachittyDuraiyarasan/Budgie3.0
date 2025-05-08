package com.hepl.budgie.entity.role;

import java.util.List;

import com.hepl.budgie.entity.menu.CommonMenuFields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmenuPermission {

    private CommonMenuFields submenu;
    private List<String> permissions;

}
