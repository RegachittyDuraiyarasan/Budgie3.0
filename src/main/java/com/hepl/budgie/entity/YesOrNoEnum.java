package com.hepl.budgie.entity;

import lombok.Getter;

@Getter
public enum YesOrNoEnum {

    YES("Yes"),
    NO("No");

    public final String label;

    private YesOrNoEnum(String label) {
        this.label = label;
    }

}
