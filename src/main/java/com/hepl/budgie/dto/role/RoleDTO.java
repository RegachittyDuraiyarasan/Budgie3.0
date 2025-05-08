package com.hepl.budgie.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RoleDTO extends RoleBaseFieldsDTO {
    private List<MenuReqDTO> permissions;
}
