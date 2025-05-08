package com.hepl.budgie.dto.role;

import lombok.Data;

import java.util.List;

@Data
public class EditRoleDTO {
    private String organizationCode;
    private List<String> roleTypes;

}
