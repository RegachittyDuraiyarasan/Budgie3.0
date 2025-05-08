package com.hepl.budgie.dto.role;

import lombok.Getter;

@Getter
public enum PermissionType {

    ADD("Add"),
    EXPORT("Export"),
    DELETE("Delete");

    public final String label;

    private PermissionType(String label) {
        this.label = label;
    }

}
