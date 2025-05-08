package com.hepl.budgie.dto.userlogin;

import lombok.Getter;

@Getter
public enum AuthSwitch {

    ROLE("Role"),
    ORGANIZATION("Organization");

    public final String label;

    private AuthSwitch(String label) {
        this.label = label;
    }

}
