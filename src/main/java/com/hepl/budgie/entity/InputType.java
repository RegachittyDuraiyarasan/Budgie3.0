package com.hepl.budgie.entity;

import lombok.Getter;

@Getter
public enum InputType {

    NUMBER("number"),
    PHONE_NUMBER("phoneNumber"),
    ALPHANUMERIC("alphaNumeric"),
    ALPHABETIC("alphabetic"),
    TEXT("text"),
    EMAIL("email");

    public final String label;

    private InputType(String label) {
        this.label = label;
    }
}
