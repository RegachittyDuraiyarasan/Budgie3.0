package com.hepl.budgie.entity.menu;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Document("menus")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Menu extends CommonMenuFields {

    @Id
    private String id;
    private String menuId;
    private int order;
    private boolean isSubmenu;
    private List<SubMenu> submenus = new ArrayList<>();

    @Transient
    private Map<String, SubMenu> submenuMap;
}
