package com.hepl.budgie.entity.master;

import lombok.Getter;

@Getter
public enum DependencyCondType {

    FETCH("Fetch"),
    DATE("Date"),
    POPULATE("Populate"),
    OPTION_EQUALITY("Option Equality"),
    CHANGE("Change");

    public final String label;

    private DependencyCondType(String label) {
        this.label = label;
    }

}
