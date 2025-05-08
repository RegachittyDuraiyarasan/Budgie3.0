package com.hepl.budgie.entity;

import java.util.HashMap;
import java.util.Map;

public enum FormFieldType {

    TEXT("text", "TextFieldServiceImpl"),
    RADIO("radio", "RadioServiceImpl"),
    CHECKBOX("checkbox", "CheckboxServiceImpl"),
    EMAIL("email", "TextFieldServiceImpl"),
    DROPDOWN("dropdown", "DropdownServiceImpl"),
    TEXTAREA("textarea", "TextAreaServiceImpl"),
    DATE("date", "DateServiceImpl"),
    COLOR("colorpicker", "ColorServiceImpl"),
    TIME("time", "TimeServiceImpl"),
    BUTTON("button", "ButtonServiceImpl"),
    FILE("file", "InputFileServiceImpl");

    public final String label;
    public final String service;
    private static final Map<String, FormFieldType> BY_LABEL = new HashMap<>();

    static {
        for (FormFieldType e : values()) {
            BY_LABEL.put(e.label, e);
        }
    }

    private FormFieldType(String label, String service) {
        this.label = label;
        this.service = service;
    }

    public static FormFieldType valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }

}
