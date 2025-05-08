package com.hepl.budgie.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmenuReqDTO {

    private String submenuName;
    private List<String> permission;

}
