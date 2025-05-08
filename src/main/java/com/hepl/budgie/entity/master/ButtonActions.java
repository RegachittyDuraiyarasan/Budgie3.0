package com.hepl.budgie.entity.master;

import lombok.Getter;

@Getter
public enum ButtonActions {

    SUBMIT("submit"),
    RESET("reset"),
    CANCEL("cancel");

    public final String label;

    private ButtonActions(String label) {
        this.label = label;
    }

}
