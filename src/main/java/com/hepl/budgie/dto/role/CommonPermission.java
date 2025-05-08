package com.hepl.budgie.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonPermission {

    private String text;
    private String path;
    private String icon;

}
