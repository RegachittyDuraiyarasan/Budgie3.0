package com.hepl.budgie.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuReqDTO {
    private String menuName;
    private List<String> permission;
    private List<SubmenuReqDTO> submenu;
}
