package com.hepl.budgie.dto.form;

import lombok.Getter;

@Getter
public enum AccessLevelType {
    EDIT("Edit"), ADD("Add");

    public final String label;

    private AccessLevelType(String label) {
        this.label = label;
    }
}
